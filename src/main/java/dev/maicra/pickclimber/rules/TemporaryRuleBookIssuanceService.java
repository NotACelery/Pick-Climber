package dev.maicra.pickclimber.rules;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import dev.maicra.pickclimber.rules.block.ClimbingRuleDispenserBlock;
import dev.maicra.pickclimber.rules.block.ClimbingRuleDispenserBlockEntity;
import dev.maicra.pickclimber.rules.item.ClimbingRuleBookData;
import dev.maicra.pickclimber.rules.item.TemporaryRuleBookData;
import dev.maicra.pickclimber.rules.persistence.RuleDefinitionLibrarySavedData;

public final class TemporaryRuleBookIssuanceService {
    public static final UUID UNCLAIMED_OWNER = new UUID(0L, 0L);
    private static final long CLAIM_WINDOW_TICKS = 20L * 60L * 5L;

    private static final Map<UUID, Issuance> BY_TOKEN = new HashMap<>();
    private static final Map<UUID, UUID> OWNER_BY_TOKEN = new HashMap<>();
    private static final Map<UUID, ClaimableIssuance> CLAIMABLE_BY_TOKEN = new HashMap<>();

    private TemporaryRuleBookIssuanceService() {
    }

    public static RuleBookIssuanceResult issue(ServerPlayer player, ClimbingRuleDispenserBlockEntity dispenser) {
        ServerLevel level = player.serverLevel();
        long gameTime = level.getServer().overworld().getGameTime();
        cleanupExpired(gameTime);
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
        if (ownsActiveDefinition(player, definitionId, gameTime)) {
            return RuleBookIssuanceResult.rejected("message.pickclimber.rules.dispenser_pending_copy");
        }

        boolean startOnPickup = dispenser.startCounterOnPickup();
        UUID token = UUID.randomUUID();
        long expiresAt = startOnPickup
                ? gameTime + CLAIM_WINDOW_TICKS
                : gameTime + lifetimeSeconds * 20L;
        UUID owner = startOnPickup ? UNCLAIMED_OWNER : player.getUUID();
        TemporaryRuleBookData.TransportData transport = transport(
                owner,
                token,
                expiresAt,
                level,
                dispenser,
                definitionId,
                definition,
                lifetimeSeconds,
                startOnPickup
        );
        ItemStack stack = TemporaryRuleBookData.create(transport);
        if (stack.isEmpty()) {
            return RuleBookIssuanceResult.rejected("message.pickclimber.rules.invalid_profile");
        }

        if (startOnPickup) {
            CLAIMABLE_BY_TOKEN.put(token, new ClaimableIssuance(token, expiresAt));
        } else {
            registerOwned(player.getUUID(), token, expiresAt, level, dispenser);
        }
        if (!spawn(level, dispenser, player.getUUID(), stack)) {
            release(token, false, level.getServer());
            return RuleBookIssuanceResult.rejected("message.pickclimber.rules.dispenser_spawn_failed");
        }
        if (!startOnPickup) {
            synchronizeOwner(player.getUUID(), level.getServer(), gameTime);
        }
        return RuleBookIssuanceResult.issued("message.pickclimber.rules.dispenser_issued");
    }

    // Redstone copies are unclaimed until pickup; the dispenser decides when their lifetime starts.
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
        boolean startOnPickup = dispenser.startCounterOnPickup();
        long expiresAt = startOnPickup
                ? gameTime + CLAIM_WINDOW_TICKS
                : gameTime + lifetimeSeconds * 20L;
        long claimDeadline = expiresAt;
        TemporaryRuleBookData.TransportData transport = transport(
                UNCLAIMED_OWNER,
                token,
                expiresAt,
                level,
                dispenser,
                definitionId,
                definition,
                lifetimeSeconds,
                startOnPickup
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
        if (claimable == null || claimable.claimDeadline() <= gameTime) {
            return false;
        }
        if (ownsActiveDefinition(player, data.definitionId(), gameTime)) {
            return false;
        }

        long expiresAt = data.startCounterOnPickup()
                ? gameTime + Math.max(1, data.durationSeconds()) * 20L
                : data.expiresAtGameTime();
        if (expiresAt <= gameTime) {
            CLAIMABLE_BY_TOKEN.remove(data.issuanceToken());
            return false;
        }
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
                data.authorName(),
                data.startCounterOnPickup()
        );
        if (!TemporaryRuleBookData.write(stack, claimed)) {
            return false;
        }

        CLAIMABLE_BY_TOKEN.remove(data.issuanceToken());
        BY_TOKEN.put(
                data.issuanceToken(),
                new Issuance(
                        player.getUUID(),
                        data.issuanceToken(),
                        expiresAt,
                        data.sourceDimension(),
                        data.sourcePosition()
                )
        );
        OWNER_BY_TOKEN.put(data.issuanceToken(), player.getUUID());
        synchronizeOwner(player.getUUID(), server, gameTime);
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
        Issuance issuance = BY_TOKEN.get(token);
        return issuance != null
                && issuance.owner().equals(owner)
                && issuance.expiresAtGameTime() > gameTime;
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
        BY_TOKEN.remove(token);
        if (owner == null) {
            return;
        }
        if (synchronize) {
            synchronizeOwner(owner, server, server.overworld().getGameTime());
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
        BY_TOKEN.entrySet().removeIf(entry -> {
            Issuance issuance = entry.getValue();
            if (!issuance.owner().equals(player.getUUID())) {
                return false;
            }
            OWNER_BY_TOKEN.remove(entry.getKey());
            return true;
        });
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
        synchronizeOwner(player.getUUID(), server, gameTime);
    }

    public static void clear() {
        BY_TOKEN.clear();
        OWNER_BY_TOKEN.clear();
        CLAIMABLE_BY_TOKEN.clear();
    }

    private static void sanitizeInventory(ServerPlayer player, long gameTime) {
        Set<String> seenDefinitions = new HashSet<>();
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
                continue;
            }
            if (!seenDefinitions.add(data.definitionId())) {
                release(data.issuanceToken(), true, player.serverLevel().getServer());
                player.getInventory().setItem(slot, ItemStack.EMPTY);
            }
        }
    }

    private static boolean ownsActiveDefinition(ServerPlayer player, String definitionId, long gameTime) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            Optional<TemporaryRuleBookData.TransportData> candidate = TemporaryRuleBookData.readValidated(
                    player.getInventory().getItem(slot)
            );
            if (candidate.isEmpty()) {
                continue;
            }
            TemporaryRuleBookData.TransportData data = candidate.get();
            if (data.owner().equals(player.getUUID())
                    && data.definitionId().equals(definitionId)
                    && data.expiresAtGameTime() > gameTime
                    && isActive(data.owner(), data.issuanceToken(), gameTime)) {
                return true;
            }
        }
        return false;
    }

    private static void cleanupExpired(long gameTime) {
        Iterator<Map.Entry<UUID, Issuance>> iterator = BY_TOKEN.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Issuance> entry = iterator.next();
            if (entry.getValue().expiresAtGameTime() <= gameTime) {
                OWNER_BY_TOKEN.remove(entry.getKey());
                iterator.remove();
            }
        }
        CLAIMABLE_BY_TOKEN.entrySet().removeIf(entry -> entry.getValue().claimDeadline() <= gameTime);
    }

    private static void synchronizeOwner(UUID owner, MinecraftServer server, long gameTime) {
        long nextExpiry = BY_TOKEN.values().stream()
                .filter(issuance -> issuance.owner().equals(owner))
                .mapToLong(Issuance::expiresAtGameTime)
                .filter(expiresAt -> expiresAt > gameTime)
                .min()
                .orElse(0L);
        ServerPlayer player = server.getPlayerList().getPlayer(owner);
        if (player != null) {
            TemporaryRuleBookSynchronization.send(player, nextExpiry);
        }
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
            int lifetimeSeconds,
            boolean startCounterOnPickup
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
                definition.authorName(),
                startCounterOnPickup
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
        BY_TOKEN.put(token, issuance);
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
