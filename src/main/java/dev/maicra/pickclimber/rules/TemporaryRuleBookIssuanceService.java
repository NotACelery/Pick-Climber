package dev.maicra.pickclimber.rules;

import dev.maicra.pickclimber.rules.block.ClimbingRuleDispenserBlock;
import dev.maicra.pickclimber.rules.block.ClimbingRuleDispenserBlockEntity;
import dev.maicra.pickclimber.rules.item.ClimbingRuleBookData;
import dev.maicra.pickclimber.rules.item.TemporaryRuleBookData;
import dev.maicra.pickclimber.rules.persistence.RuleDefinitionLibrarySavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class TemporaryRuleBookIssuanceService {
    public static final UUID UNCLAIMED_OWNER = new UUID(0L, 0L);
    private static final long CLAIM_WINDOW_TICKS = 20L * 60L * 5L;

    private static final Map<UUID, Issuance> BY_PLAYER = new HashMap<>();
    private static final Map<UUID, UUID> OWNER_BY_TOKEN = new HashMap<>();
    private static final Map<UUID, ClaimableIssuance> CLAIMABLE_BY_TOKEN = new HashMap<>();

    private TemporaryRuleBookIssuanceService() {
    }

    public static RuleBookIssuanceResult issue(ServerPlayer player, ClimbingRuleDispenserBlockEntity dispenser) {
        ServerLevel level = player.serverLevel();
        long gameTime = level.getServer().overworld().getGameTime();
        cleanupExpired(gameTime);
        if (BY_PLAYER.containsKey(player.getUUID())) {
            return RuleBookIssuanceResult.rejected("message.pickclimber.rules.dispenser_pending_copy");
        }

        Optional<ClimbingRuleBookDefinition> source = resolveSource(level.getServer(), dispenser);
        if (source.isEmpty()) {
            return RuleBookIssuanceResult.rejected("message.pickclimber.rules.valid_book_required");
        }

        int lifetimeSeconds = lifetime(dispenser);
        ClimbingRuleBookDefinition definition = temporaryCopy(source.get(), lifetimeSeconds);
        if (ClimbingRulesService.isAlreadyEffectiveFor(player, definition)) {
            return RuleBookIssuanceResult.rejected("message.pickclimber.rules.no_effective_change");
        }

        String definitionId = register(level.getServer(), definition);
        if (definitionId.isEmpty()) {
            return RuleBookIssuanceResult.rejected("message.pickclimber.rules.invalid_profile");
        }

        long expiresAt = gameTime + lifetimeSeconds * 20L;
        UUID token = UUID.randomUUID();
        TemporaryRuleBookData.TransportData transport = transport(
                player.getUUID(), token, expiresAt, level, dispenser, definitionId, definition, lifetimeSeconds
        );
        ItemStack stack = TemporaryRuleBookData.create(transport);
        if (stack.isEmpty()) {
            return RuleBookIssuanceResult.rejected("message.pickclimber.rules.invalid_profile");
        }

        registerOwned(player.getUUID(), token, expiresAt, level, dispenser);
        if (!spawn(level, dispenser, player.getUUID(), stack)) {
            release(token, false, level.getServer());
            return RuleBookIssuanceResult.rejected("message.pickclimber.rules.dispenser_spawn_failed");
        }
        TemporaryRuleBookSynchronization.send(player, expiresAt);
        return RuleBookIssuanceResult.issued("message.pickclimber.rules.dispenser_issued");
    }

    /**
     * Redstone path: dispense an unclaimed copy. The first eligible player to pick it up becomes its owner and the
     * configured lifetime starts at pickup time. This keeps redstone automation independent from whoever opened
     * the GUI.
     */
    public static RuleBookIssuanceResult dispense(ServerLevel level, ClimbingRuleDispenserBlockEntity dispenser) {
        long gameTime = level.getServer().overworld().getGameTime();
        cleanupExpired(gameTime);
        Optional<ClimbingRuleBookDefinition> source = resolveSource(level.getServer(), dispenser);
        if (source.isEmpty()) {
            return RuleBookIssuanceResult.rejected("message.pickclimber.rules.valid_book_required");
        }

        int lifetimeSeconds = lifetime(dispenser);
        ClimbingRuleBookDefinition definition = temporaryCopy(source.get(), lifetimeSeconds);
        String definitionId = register(level.getServer(), definition);
        if (definitionId.isEmpty()) {
            return RuleBookIssuanceResult.rejected("message.pickclimber.rules.invalid_profile");
        }

        UUID token = UUID.randomUUID();
        long claimDeadline = gameTime + CLAIM_WINDOW_TICKS;
        TemporaryRuleBookData.TransportData transport = transport(
                UNCLAIMED_OWNER,
                token,
                claimDeadline,
                level,
                dispenser,
                definitionId,
                definition,
                lifetimeSeconds
        );
        ItemStack stack = TemporaryRuleBookData.create(transport);
        if (stack.isEmpty()) {
            return RuleBookIssuanceResult.rejected("message.pickclimber.rules.invalid_profile");
        }

        CLAIMABLE_BY_TOKEN.put(token, new ClaimableIssuance(token, claimDeadline));
        if (!spawn(level, dispenser, null, stack)) {
            CLAIMABLE_BY_TOKEN.remove(token);
            return RuleBookIssuanceResult.rejected("message.pickclimber.rules.dispenser_spawn_failed");
        }
        return RuleBookIssuanceResult.issued("message.pickclimber.rules.dispenser_issued");
    }

    public static boolean claim(ItemStack stack, ServerPlayer player) {
        Optional<TemporaryRuleBookData.TransportData> dataOptional = TemporaryRuleBookData.readValidated(stack);
        if (dataOptional.isEmpty()) {
            return false;
        }
        TemporaryRuleBookData.TransportData data = dataOptional.get();
        if (!UNCLAIMED_OWNER.equals(data.owner())) {
            return data.owner().equals(player.getUUID());
        }

        MinecraftServer server = player.serverLevel().getServer();
        long gameTime = server.overworld().getGameTime();
        cleanupExpired(gameTime);
        ClaimableIssuance claimable = CLAIMABLE_BY_TOKEN.get(data.issuanceToken());
        if (claimable == null || claimable.claimDeadline() <= gameTime || BY_PLAYER.containsKey(player.getUUID())) {
            return false;
        }

        long expiresAt = gameTime + Math.max(1, data.durationSeconds()) * 20L;
        TemporaryRuleBookData.TransportData claimed = new TemporaryRuleBookData.TransportData(
                player.getUUID(),
                data.issuanceToken(),
                expiresAt,
                data.sourceDimension(),
                data.sourcePosition(),
                data.definitionId(),
                data.bookName(),
                data.coverColor(),
                data.durationSeconds(),
                data.authorUuid(),
                data.authorName()
        );
        if (!TemporaryRuleBookData.write(stack, claimed)) {
            return false;
        }

        CLAIMABLE_BY_TOKEN.remove(data.issuanceToken());
        BY_PLAYER.put(
                player.getUUID(),
                new Issuance(
                        player.getUUID(),
                        data.issuanceToken(),
                        expiresAt,
                        data.sourceDimension(),
                        data.sourcePosition()
                )
        );
        OWNER_BY_TOKEN.put(data.issuanceToken(), player.getUUID());
        TemporaryRuleBookSynchronization.send(player, expiresAt);
        return true;
    }

    public static boolean isUnclaimed(TemporaryRuleBookData.TransportData data) {
        return data != null && UNCLAIMED_OWNER.equals(data.owner());
    }

    public static boolean isActive(UUID owner, UUID token, long gameTime) {
        cleanupExpired(gameTime);
        if (UNCLAIMED_OWNER.equals(owner)) {
            ClaimableIssuance claimable = CLAIMABLE_BY_TOKEN.get(token);
            return claimable != null && claimable.claimDeadline() > gameTime;
        }
        Issuance issuance = BY_PLAYER.get(owner);
        return issuance != null && issuance.token().equals(token) && issuance.expiresAtGameTime() > gameTime;
    }

    public static void releaseFromStack(ItemStack stack, MinecraftServer server) {
        TemporaryRuleBookData.readValidated(stack).ifPresent(data -> {
            if (isUnclaimed(data)) {
                CLAIMABLE_BY_TOKEN.remove(data.issuanceToken());
            } else {
                release(data.issuanceToken(), true, server);
            }
        });
    }

    public static void release(UUID token, boolean synchronize, MinecraftServer server) {
        CLAIMABLE_BY_TOKEN.remove(token);
        UUID owner = OWNER_BY_TOKEN.remove(token);
        if (owner == null) {
            return;
        }
        Issuance issuance = BY_PLAYER.get(owner);
        if (issuance != null && issuance.token().equals(token)) {
            BY_PLAYER.remove(owner);
        }
        if (synchronize) {
            ServerPlayer player = server.getPlayerList().getPlayer(owner);
            if (player != null) {
                TemporaryRuleBookSynchronization.send(player, 0L);
            }
        }
    }

    public static void removeOwnedBooks(ServerPlayer player) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            Optional<TemporaryRuleBookData.TransportData> dataOptional = TemporaryRuleBookData.readValidated(stack);
            if (dataOptional.isEmpty()) {
                continue;
            }
            TemporaryRuleBookData.TransportData data = dataOptional.get();
            if (data.owner().equals(player.getUUID())) {
                release(data.issuanceToken(), false, player.serverLevel().getServer());
                player.getInventory().setItem(slot, ItemStack.EMPTY);
            }
        }
        Issuance issuance = BY_PLAYER.remove(player.getUUID());
        if (issuance != null) {
            OWNER_BY_TOKEN.remove(issuance.token());
        }
        TemporaryRuleBookSynchronization.send(player, 0L);
    }

    public static void tick(MinecraftServer server) {
        long gameTime = server.overworld().getGameTime();
        cleanupExpired(gameTime);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            sanitizeInventory(player, gameTime);
        }
    }

    public static void syncPlayer(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }
        long gameTime = server.overworld().getGameTime();
        cleanupExpired(gameTime);
        Issuance issuance = BY_PLAYER.get(player.getUUID());
        long expiresAt = issuance == null ? 0L : issuance.expiresAtGameTime();
        TemporaryRuleBookSynchronization.send(player, expiresAt);
    }

    public static void clear() {
        BY_PLAYER.clear();
        OWNER_BY_TOKEN.clear();
        CLAIMABLE_BY_TOKEN.clear();
    }

    private static void sanitizeInventory(ServerPlayer player, long gameTime) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            Optional<TemporaryRuleBookData.TransportData> dataOptional = TemporaryRuleBookData.readValidated(stack);
            if (dataOptional.isEmpty()) {
                continue;
            }
            TemporaryRuleBookData.TransportData data = dataOptional.get();
            if (isUnclaimed(data)) {
                if (!claim(stack, player)) {
                    player.getInventory().setItem(slot, ItemStack.EMPTY);
                }
                continue;
            }
            if (!data.owner().equals(player.getUUID())) {
                player.getInventory().setItem(slot, ItemStack.EMPTY);
                continue;
            }
            if (data.expiresAtGameTime() <= gameTime || !isActive(data.owner(), data.issuanceToken(), gameTime)) {
                release(data.issuanceToken(), true, player.serverLevel().getServer());
                player.getInventory().setItem(slot, ItemStack.EMPTY);
            }
        }
    }

    private static void cleanupExpired(long gameTime) {
        Iterator<Map.Entry<UUID, Issuance>> iterator = BY_PLAYER.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Issuance> entry = iterator.next();
            if (entry.getValue().expiresAtGameTime() <= gameTime) {
                OWNER_BY_TOKEN.remove(entry.getValue().token());
                iterator.remove();
            }
        }
        CLAIMABLE_BY_TOKEN.entrySet().removeIf(entry -> entry.getValue().claimDeadline() <= gameTime);
    }

    private static Optional<ClimbingRuleBookDefinition> resolveSource(
            MinecraftServer server,
            ClimbingRuleDispenserBlockEntity dispenser
    ) {
        return ClimbingRuleBookData.resolveDefinition(server, dispenser.getSource());
    }

    private static int lifetime(ClimbingRuleDispenserBlockEntity dispenser) {
        return Math.max(1, Math.min(60, dispenser.getLifetimeSeconds()));
    }

    private static String register(MinecraftServer server, ClimbingRuleBookDefinition definition) {
        return RuleDefinitionLibrarySavedData.get(server).register(definition.profile());
    }

    private static TemporaryRuleBookData.TransportData transport(
            UUID owner,
            UUID token,
            long expiresAt,
            ServerLevel level,
            ClimbingRuleDispenserBlockEntity dispenser,
            String definitionId,
            ClimbingRuleBookDefinition definition,
            int lifetimeSeconds
    ) {
        return new TemporaryRuleBookData.TransportData(
                owner,
                token,
                expiresAt,
                level.dimension().location(),
                dispenser.getBlockPos(),
                definitionId,
                definition.bookName(),
                definition.coverColor(),
                lifetimeSeconds,
                definition.authorUuid(),
                definition.authorName()
        );
    }

    private static void registerOwned(
            UUID owner,
            UUID token,
            long expiresAt,
            ServerLevel level,
            ClimbingRuleDispenserBlockEntity dispenser
    ) {
        Issuance issuance = new Issuance(
                owner,
                token,
                expiresAt,
                level.dimension().location(),
                dispenser.getBlockPos()
        );
        BY_PLAYER.put(owner, issuance);
        OWNER_BY_TOKEN.put(token, owner);
    }

    private static ClimbingRuleBookDefinition temporaryCopy(
            ClimbingRuleBookDefinition source,
            int lifetimeSeconds
    ) {
        return new ClimbingRuleBookDefinition(
                source.formatVersion(),
                source.bookName(),
                source.coverColor(),
                source.profile(),
                RuleBookActivationMode.TEMPORARY,
                RuleBookScope.PLAYER,
                lifetimeSeconds,
                source.authorUuid(),
                source.authorName()
        );
    }

    private static boolean spawn(
            ServerLevel level,
            ClimbingRuleDispenserBlockEntity dispenser,
            UUID target,
            ItemStack stack
    ) {
        Direction facing = dispenser.getBlockState().getValue(ClimbingRuleDispenserBlock.FACING);
        BlockPos pos = dispenser.getBlockPos();
        double x = pos.getX() + 0.5D + facing.getStepX() * 0.7D;
        double y = pos.getY() + 0.5D + facing.getStepY() * 0.7D;
        double z = pos.getZ() + 0.5D + facing.getStepZ() * 0.7D;
        ItemEntity item = new ItemEntity(level, x, y, z, stack);
        if (target != null) {
            item.setTarget(target);
        }
        item.setPickUpDelay(8);
        item.setDeltaMovement(
                facing.getStepX() * 0.18D,
                facing.getStepY() * 0.18D + 0.04D,
                facing.getStepZ() * 0.18D
        );
        return level.addFreshEntity(item);
    }

    private record Issuance(
            UUID owner,
            UUID token,
            long expiresAtGameTime,
            ResourceLocation sourceDimension,
            BlockPos sourcePosition
    ) {
    }

    private record ClaimableIssuance(UUID token, long claimDeadline) {
    }
}
