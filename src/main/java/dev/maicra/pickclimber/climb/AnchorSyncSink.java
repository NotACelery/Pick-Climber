package dev.maicra.pickclimber.climb;

import java.util.UUID;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;

public interface AnchorSyncSink {
    void sendAttached(ServerPlayer player, ServerClimbState state, boolean newAnchor);

    void sendDetached(
            ServerPlayer player,
            boolean restoreNoGravity,
            boolean restoreFlying,
            boolean jump,
            UUID toolId,
            int cooldownTicks,
            boolean refundCooldown
    );

    void sendBoost(ServerPlayer player, Vec3 velocity, InteractionHand hand, int cooldownTicks);

    void sendRemotePose(ServerPlayer player, ServerClimbState state);

    void sendRemotePoseDetached(ServerPlayer player);
}
