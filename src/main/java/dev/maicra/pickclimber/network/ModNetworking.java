package dev.maicra.pickclimber.network;

import dev.maicra.pickclimber.climb.ClimbManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetworking {
    private ModNetworking() {
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("13");

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
    }

    private static void handleAnchorSync(AnchorSyncPayload payload, IPayloadContext context) {
        Player player = context.player();
        ClimbManager.applyClientSync(player, payload);
    }

    private static void handleBoostSync(BoostSyncPayload payload, IPayloadContext context) {
        Player player = context.player();
        ClimbManager.applyClientBoost(player, payload);
    }

    private static void handleRemoteAnchorPose(RemoteAnchorPosePayload payload, IPayloadContext context) {
        ClimbManager.applyRemoteAnchorPose(context.player(), payload);
    }

    private static void handleDetachRequest(DetachRequestPayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer serverPlayer) {
            // The input that triggered Space travels in the same request so the
            // jump vector does not depend on the periodic payload's tick.
            ClimbManager.updateSlideInput(serverPlayer, new SlideInputPayload(
                    payload.forward(),
                    payload.strafe(),
                    payload.yaw(),
                    payload.pitch()
            ));
            ClimbManager.detachServer(serverPlayer, payload.jump());
        }
    }

    private static void handleSlideInput(SlideInputPayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer serverPlayer) {
            ClimbManager.updateSlideInput(serverPlayer, payload);
        }
    }
}
