package dev.maicra.pickclimber.rules;

import dev.maicra.pickclimber.rules.block.ClimbingRuleDispenserBlock;
import dev.maicra.pickclimber.rules.block.ClimbingRuleDispenserBlockEntity;
import dev.maicra.pickclimber.rules.item.ClimbingRuleBookData;
import dev.maicra.pickclimber.rules.item.TemporaryRuleBookData;
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
    private static final Map<UUID, Issuance> BY_PLAYER = new HashMap<>();
    private static final Map<UUID, UUID> OWNER_BY_TOKEN = new HashMap<>();

    private TemporaryRuleBookIssuanceService() {
    }

    public static RuleBookIssuanceResult issue(ServerPlayer player, ClimbingRuleDispenserBlockEntity dispenser) {
        ServerLevel level = player.serverLevel();
        long gameTime = level.getServer().overworld().getGameTime();
        cleanupExpired(gameTime);
        if (BY_PLAYER.containsKey(player.getUUID())) {
            return RuleBookIssuanceResult.rejected("message.pickclimber.rules.dispenser_pending_copy");
        }

        Optional<ClimbingRuleBookDefinition> master = ClimbingRuleBookData.readDefinitionValidated(
                dispenser.getMaster()
        );
        if (master.isEmpty() || master.get().activationMode() != RuleBookActivationMode.TEMPORARY) {
            return RuleBookIssuanceResult.rejected("message.pickclimber.rules.dispenser_temporary_required");
        }
        ClimbingRuleBookDefinition definition = master.get();
        if (ClimbingRulesService.isAlreadyEffectiveFor(player, definition)) {
            return RuleBookIssuanceResult.rejected("message.pickclimber.rules.no_effective_change");
        }

        int lifetimeSeconds = Math.max(1, Math.min(60, dispenser.getLifetimeSeconds()));
        long expiresAt = gameTime + lifetimeSeconds * 20L;
        UUID token = UUID.randomUUID();
        ResourceLocation dimension = level.dimension().location();
        BlockPos sourcePosition = dispenser.getBlockPos();
        TemporaryRuleBookData.TransportData transport = new TemporaryRuleBookData.TransportData(
                player.getUUID(), token, expiresAt, dimension, sourcePosition, definition
        );
        ItemStack stack = TemporaryRuleBookData.create(transport);
        if (stack.isEmpty()) {
            return RuleBookIssuanceResult.rejected("message.pickclimber.rules.invalid_profile");
        }

        Issuance issuance = new Issuance(player.getUUID(), token, expiresAt, dimension, sourcePosition);
        BY_PLAYER.put(player.getUUID(), issuance);
        OWNER_BY_TOKEN.put(token, player.getUUID());
        if (!spawn(level, dispenser, player, stack)) {
            release(token, false, level.getServer());
            return RuleBookIssuanceResult.rejected("message.pickclimber.rules.dispenser_spawn_failed");
        }
        TemporaryRuleBookSynchronization.send(player, expiresAt);
        return RuleBookIssuanceResult.issued("message.pickclimber.rules.dispenser_issued");
    }

    public static boolean isActive(UUID owner, UUID token, long gameTime) {
        cleanupExpired(gameTime);
        Issuance issuance = BY_PLAYER.get(owner);
        return issuance != null && issuance.token().equals(token) && issuance.expiresAtGameTime() > gameTime;
    }

    public static void releaseFromStack(ItemStack stack, MinecraftServer server) {
        TemporaryRuleBookData.readValidated(stack).ifPresent(data -> release(data.issuanceToken(), true, server));
    }

    public static void release(UUID token, boolean synchronize, MinecraftServer server) {
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
    }

    private static void sanitizeInventory(ServerPlayer player, long gameTime) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            Optional<TemporaryRuleBookData.TransportData> dataOptional = TemporaryRuleBookData.readValidated(stack);
            if (dataOptional.isEmpty()) {
                continue;
            }
            TemporaryRuleBookData.TransportData data = dataOptional.get();
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
    }

    private static boolean spawn(
            ServerLevel level,
            ClimbingRuleDispenserBlockEntity dispenser,
            ServerPlayer owner,
            ItemStack stack
    ) {
        Direction facing = dispenser.getBlockState().getValue(ClimbingRuleDispenserBlock.FACING);
        BlockPos pos = dispenser.getBlockPos();
        double x = pos.getX() + 0.5D + facing.getStepX() * 0.7D;
        double y = pos.getY() + 0.5D + facing.getStepY() * 0.7D;
        double z = pos.getZ() + 0.5D + facing.getStepZ() * 0.7D;
        ItemEntity item = new ItemEntity(level, x, y, z, stack);
        item.setTarget(owner.getUUID());
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
}
