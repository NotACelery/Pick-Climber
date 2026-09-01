package dev.maicra.pickclimber.climb;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.phys.Vec3;

final class AnchorPositioning {
    private AnchorPositioning() {
    }

    static void holdPlayer(ServerPlayer player, ServerClimbState state) {
        Vec3 target = state.targetPosition();
        player.setNoGravity(true);
        player.fallDistance = 0.0F;
        player.setOnGround(false);
        player.setDeltaMovement(Vec3.ZERO);

        double correctionThresholdSqr = state.anchorFace() == Direction.DOWN
                ? ClimbTuning.CEILING_HOLD_CORRECTION_DISTANCE_SQR
                : ClimbTuning.WALL_HOLD_CORRECTION_DISTANCE_SQR;
        if (player.position().distanceToSqr(target) > correctionThresholdSqr) {
            boolean smoothCeilingStep = state.anchorFace() == Direction.DOWN
                    && player.tickCount % ClimbTuning.SERVER_SYNC_INTERVAL != 0;
            if (smoothCeilingStep) {
                player.setPos(target);
            } else {
                player.connection.teleport(
                        target.x,
                        target.y,
                        target.z,
                        player.getYRot(),
                        player.getXRot(),
                        RelativeMovement.ROTATION
                );
            }
            player.setDeltaMovement(Vec3.ZERO);
        }
    }
}
