package dev.maicra.pickclimber.climb;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;

final class AnchorMotionService {
    private AnchorMotionService() {
    }

    static ServerClimbState advance(ServerPlayer player, ServerClimbState state) {
        if (player.level().getGameTime() <= state.attachedAtGameTime()) {
            return state;
        }
        if (state.anchorFace() == Direction.DOWN) {
            return CeilingAnchorMotion.advance(player, state);
        }
        return WallAnchorMotion.advance(player, state);
    }
}
