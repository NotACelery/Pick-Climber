package dev.maicra.pickclimber.network;

import java.util.UUID;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import dev.maicra.pickclimber.climb.AnchorSyncSink;
import dev.maicra.pickclimber.climb.ServerClimbState;

final class NeoForgeAnchorSyncSink implements AnchorSyncSink {
    static final NeoForgeAnchorSyncSink INSTANCE = new NeoForgeAnchorSyncSink();

    private NeoForgeAnchorSyncSink() {
    }

    @Override
    public void sendAttached(ServerPlayer player, ServerClimbState state, boolean newAnchor) {
        PacketDistributor.sendToPlayer(player, AnchorSyncPayload.attached(state, newAnchor));
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
        PacketDistributor.sendToPlayer(
                player,
                AnchorSyncPayload.detached(
                        restoreNoGravity,
                        restoreFlying,
                        jump,
                        toolId,
                        cooldownTicks,
                        refundCooldown
                )
        );
    }

    @Override
    public void sendBoost(ServerPlayer player, Vec3 velocity, InteractionHand hand, int cooldownTicks) {
        PacketDistributor.sendToPlayer(
                player,
                new BoostSyncPayload(
                        velocity.x,
                        velocity.y,
                        velocity.z,
                        hand.ordinal(),
                        cooldownTicks
                )
        );
    }

    @Override
    public void sendRemotePose(ServerPlayer player, ServerClimbState state) {
        RemoteAnchorPosePayload payload = state.anchorFace() == Direction.DOWN
                ? RemoteAnchorPosePayload.attached(player.getUUID(), state.activeHand())
                : RemoteAnchorPosePayload.detached(player.getUUID());
        PacketDistributor.sendToPlayersTrackingEntity(player, payload);
    }

    @Override
    public void sendRemotePoseDetached(ServerPlayer player) {
        PacketDistributor.sendToAllPlayers(RemoteAnchorPosePayload.detached(player.getUUID()));
    }
}
