package dev.maicra.pickclimber.climb;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;

final class AnchorInputStateService {
    private AnchorInputStateService() {
    }

    static void update(
            ServerPlayer player,
            float forwardInput,
            float strafeInput,
            float yaw,
            float pitch
    ) {
        ServerClimbState state = ClimbStateStore.server(player.getUUID());
        if (state == null
                || !Float.isFinite(forwardInput)
                || !Float.isFinite(strafeInput)
                || !Float.isFinite(yaw)
                || !Float.isFinite(pitch)) {
            return;
        }

        float forward = Mth.clamp(forwardInput, -1.0F, 1.0F);
        float strafe = Mth.clamp(strafeInput, -1.0F, 1.0F);
        player.setYRot(Mth.wrapDegrees(yaw));
        player.setXRot(Mth.clamp(pitch, -90.0F, 90.0F));
        ClimbStateStore.putServer(player.getUUID(), state.withSlideInput(forward, strafe));
    }
}
