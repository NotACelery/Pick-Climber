package dev.maicra.pickclimber.network;

import dev.maicra.pickclimber.climb.ClientAnchorSync;
import dev.maicra.pickclimber.climb.ClientClimbSynchronizer;
import dev.maicra.pickclimber.climb.ClimbManager;
import dev.maicra.pickclimber.climb.ClimbRuntimeGate;
import dev.maicra.pickclimber.climb.PlayerClimbPresentationPreferences;
import dev.maicra.pickclimber.climb.PlayerClimbRuntimePreferences;
import dev.maicra.pickclimber.climb.ClimbSynchronization;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetworking {
    private ModNetworking() {
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        ClimbSynchronization.install(NeoForgeAnchorSyncSink.INSTANCE);
        PayloadRegistrar registrar = event.registrar("14");

        registrar.playToClient(
                AnchorSyncPayload.TYPE,
                AnchorSyncPayload.STREAM_CODEC,
                ModNetworking::handleAnchorSync
        );
        registrar.playToClient(
                BoostSyncPayload.TYPE,
                BoostSyncPayload.STREAM_CODEC,
                ModNetworking::handleBoostSync
        );
        registrar.playToClient(
                RemoteAnchorPosePayload.TYPE,
                RemoteAnchorPosePayload.STREAM_CODEC,
                ModNetworking::handleRemoteAnchorPose
        );
        registrar.playToServer(
                DetachRequestPayload.TYPE,
                DetachRequestPayload.STREAM_CODEC,
                ModNetworking::handleDetachRequest
        );
        registrar.playToServer(
                SlideInputPayload.TYPE,
                SlideInputPayload.STREAM_CODEC,
                ModNetworking::handleSlideInput
        );
        registrar.playToServer(
                RuntimePreferencePayload.TYPE,
                RuntimePreferencePayload.STREAM_CODEC,
                ModNetworking::handleRuntimePreference
        );
    }

    private static void handleAnchorSync(AnchorSyncPayload payload, IPayloadContext context) {
        Player player = context.player();
        InteractionHand hand = hand(payload.handOrdinal());
        ClientClimbSynchronizer.applyAnchor(
                player,
                new ClientAnchorSync(
                        payload.attached(),
                        new Vec3(payload.targetX(), payload.targetY(), payload.targetZ()),
                        new BlockPos(payload.anchorX(), payload.anchorY(), payload.anchorZ()),
                        payload.crackId(),
                        hand,
                        payload.toolId(),
                        payload.restoreNoGravity(),
                        payload.restoreFlying(),
                        payload.jumpDetach(),
                        payload.newAnchor(),
                        payload.refundCooldown(),
                        payload.ceilingAnchor(),
                        payload.cooldownTicks()
                )
        );
    }

    private static void handleBoostSync(BoostSyncPayload payload, IPayloadContext context) {
        ClientClimbSynchronizer.applyBoost(
                context.player(),
                new Vec3(payload.velocityX(), payload.velocityY(), payload.velocityZ()),
                hand(payload.handOrdinal()),
                payload.cooldownTicks()
        );
    }

    private static void handleRemoteAnchorPose(RemoteAnchorPosePayload payload, IPayloadContext context) {
        ClientClimbSynchronizer.applyRemotePose(
                context.player(),
                payload.playerId(),
                payload.ceilingAnchor(),
                hand(payload.handOrdinal())
        );
    }

    private static void handleDetachRequest(DetachRequestPayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer serverPlayer
                && ClimbRuntimeGate.interactionsEnabled(serverPlayer)) {
            ClimbManager.updateSlideInput(
                    serverPlayer,
                    payload.forward(),
                    payload.strafe(),
                    payload.yaw(),
                    payload.pitch()
            );
            ClimbManager.detachServer(serverPlayer, payload.jump());
        }
    }

    private static void handleSlideInput(SlideInputPayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer serverPlayer
                && ClimbRuntimeGate.interactionsEnabled(serverPlayer)) {
            ClimbManager.updateSlideInput(
                    serverPlayer,
                    payload.forward(),
                    payload.strafe(),
                    payload.yaw(),
                    payload.pitch()
            );
        }
    }

    private static void handleRuntimePreference(RuntimePreferencePayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer serverPlayer)) {
            return;
        }

        PlayerClimbRuntimePreferences.setInteractionsEnabled(serverPlayer, payload.interactionsEnabled());
        PlayerClimbPresentationPreferences.setFailureTextEnabled(serverPlayer, payload.failureTextEnabled());
        if (!payload.interactionsEnabled() && ClimbManager.isAttached(serverPlayer)) {
            ClimbManager.detachServer(serverPlayer, false);
        }
    }

    private static InteractionHand hand(int ordinal) {
        return ordinal == InteractionHand.OFF_HAND.ordinal()
                ? InteractionHand.OFF_HAND
                : InteractionHand.MAIN_HAND;
    }
}
