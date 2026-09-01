package dev.maicra.pickclimber.climb;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

final class CeilingAnchorMotion {
    private CeilingAnchorMotion() {
    }

    static ServerClimbState advance(ServerPlayer player, ServerClimbState state) {
        Vec3 center = state.ceilingCenter();
        Vec3 horizontalOffset = new Vec3(
                state.targetPosition().x - center.x,
                0.0D,
                state.targetPosition().z - center.z
        );
        Vec3 input = inputDirection(player, state);
        Vec3 acceleration = input.scale(ClimbTuning.CEILING_SWING_ACCELERATION)
                .add(horizontalOffset.scale(-ClimbTuning.CEILING_SWING_RETURN));
        Vec3 velocity = new Vec3(state.swingVelocity().x, 0.0D, state.swingVelocity().z)
                .add(acceleration)
                .scale(ClimbTuning.CEILING_SWING_DAMPING);
        Vec3 releaseMomentum = new Vec3(
                state.swingReleaseMomentum().x,
                0.0D,
                state.swingReleaseMomentum().z
        ).add(acceleration).scale(ClimbTuning.CEILING_SWING_DAMPING);

        if (velocity.length() > ClimbTuning.CEILING_SWING_MAX_SPEED) {
            velocity = velocity.normalize().scale(ClimbTuning.CEILING_SWING_MAX_SPEED);
        }
        if (releaseMomentum.length() > ClimbTuning.CEILING_RELEASE_MOMENTUM_MAX_SPEED) {
            releaseMomentum = releaseMomentum.normalize().scale(
                    ClimbTuning.CEILING_RELEASE_MOMENTUM_MAX_SPEED
            );
        }

        Vec3 nextOffset = horizontalOffset.add(velocity);
        if (nextOffset.length() > ClimbTuning.CEILING_SWING_RADIUS) {
            nextOffset = nextOffset.normalize().scale(ClimbTuning.CEILING_SWING_RADIUS);
            double outwardSpeed = velocity.dot(nextOffset.normalize());
            if (outwardSpeed > 0.0D) {
                velocity = velocity.subtract(nextOffset.normalize().scale(outwardSpeed));
            }
        }

        Vec3 nextTarget = new Vec3(
                center.x + nextOffset.x,
                center.y,
                center.z + nextOffset.z
        );
        Vec3 displacement = nextTarget.subtract(player.position());
        if (!player.level().noCollision(player, player.getBoundingBox().move(displacement))) {
            return state.withCeilingSwing(state.targetPosition(), Vec3.ZERO, Vec3.ZERO);
        }
        return state.withCeilingSwing(nextTarget, velocity, releaseMomentum);
    }

    static Vec3 inputDirection(ServerPlayer player, ServerClimbState state) {
        Vec3 look = player.getLookAngle();
        Vec3 forward = new Vec3(look.x, 0.0D, look.z);
        if (forward.lengthSqr() < ClimbTuning.DIRECTION_EPSILON_SQR) {
            return Vec3.ZERO;
        }
        forward = forward.normalize();
        Vec3 right = new Vec3(-forward.z, 0.0D, forward.x);
        Vec3 input = forward.scale(state.lateralForward())
                .add(right.scale(-state.lateralStrafe()));
        return input.lengthSqr() > 1.0D ? input.normalize() : input;
    }
}
