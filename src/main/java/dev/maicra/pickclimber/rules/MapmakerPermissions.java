package dev.maicra.pickclimber.rules;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class MapmakerPermissions {
    public static final int REQUIRED_PERMISSION_LEVEL = 2;
    public static final double MAX_TERMINAL_DISTANCE_SQR = 64.0D;

    private MapmakerPermissions() {
    }

    public static boolean canManage(Player player) {
        return player.isCreative() && player.hasPermissions(REQUIRED_PERMISSION_LEVEL);
    }

    public static boolean canBypassMiningLock(Player player) {
        return canManage(player);
    }

    public static boolean isNear(ServerPlayer player, net.minecraft.core.BlockPos position) {
        return player.distanceToSqr(
                position.getX() + 0.5D,
                position.getY() + 0.5D,
                position.getZ() + 0.5D
        ) <= MAX_TERMINAL_DISTANCE_SQR;
    }
}
