package dev.maicra.pickclimber.climb;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public final class ClimbSynchronization {
    private static final AnchorSyncSink NO_OP = new AnchorSyncSink() {
        @Override
        public void sendAttached(ServerPlayer player, ServerClimbState state, boolean newAnchor) {
        }

        @Override
        public void sendDetached(
                ServerPlayer player,
                boolean restoreNoGravity,
                boolean restoreFlying,
                boolean jump,
                UUID toolId,
                int cooldownTicks,
                boolean refundCooldown
        ) {
        }

        @Override
        public void sendBoost(ServerPlayer player, Vec3 velocity, InteractionHand hand, int cooldownTicks) {
        }

        @Override
        public void sendRemotePose(ServerPlayer player, ServerClimbState state) {
        }

        @Override
        public void sendRemotePoseDetached(ServerPlayer player) {
        }
    };

    private static AnchorSyncSink sink = NO_OP;

    private ClimbSynchronization() {
    }

    public static void install(AnchorSyncSink nextSink) {
        sink = nextSink == null ? NO_OP : nextSink;
    }

    static void sendAttached(ServerPlayer player, ServerClimbState state, boolean newAnchor) {
        sink.sendAttached(player, state, newAnchor);
    }

    static void sendDetached(
            ServerPlayer player,
            boolean restoreNoGravity,
            boolean restoreFlying,
            boolean jump,
            UUID toolId,
            int cooldownTicks,
            boolean refundCooldown
    ) {
        sink.sendDetached(
                player,
                restoreNoGravity,
                restoreFlying,
                jump,
                toolId,
                cooldownTicks,
                refundCooldown
        );
    }

    static void sendBoost(ServerPlayer player, Vec3 velocity, InteractionHand hand, int cooldownTicks) {
        sink.sendBoost(player, velocity, hand, cooldownTicks);
    }

    static void sendRemotePose(ServerPlayer player, ServerClimbState state) {
        sink.sendRemotePose(player, state);
    }

    static void sendRemotePoseDetached(ServerPlayer player) {
        sink.sendRemotePoseDetached(player);
    }
}
