package dev.maicra.pickclimber.climb;

import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

final class WallAnchorMotion {
    private WallAnchorMotion() {
    }

    static ServerClimbState advance(ServerPlayer player, ServerClimbState state) {
        if (state.motion() == AnchorMotion.FIXED) {
            return state;
        }
        if (state.motion() == AnchorMotion.BRAKING
                && state.brakingSupportToolId() != null
                && !hasBrakingSupport(player, state)) {
            state = state.withoutBrakingSupport();
        }

        AnchorMotion nextMotion = state.motion();
        double nextVelocity = state.slideVelocity();
        if (state.motion() == AnchorMotion.BRAKING) {
            int brakingSteps = hasBrakingSupport(player, state)
                    ? ClimbTuning.BRAKING_SUPPORT_STEPS
                    : 1;
            for (int step = 0; step < brakingSteps; step++) {
                nextVelocity = Math.min(
                        0.0D,
                        nextVelocity * ClimbTuning.BRAKING_DRAG + ClimbTuning.BRAKING_RECOVERY
                );
            }
            if (nextVelocity > ClimbTuning.BRAKING_STOP_SPEED) {
                if (state.surface() == AnchorSurface.UNSTABLE && !state.reinforcedLatch()) {
                    nextMotion = AnchorMotion.UNSTABLE_SLIDING;
                    nextVelocity = ClimbTuning.UNSTABLE_SLIDE_SPEED;
                } else {
                    return state.withMotion(state.targetPosition(), AnchorMotion.FIXED, 0.0D);
                }
            }
        } else {
            nextVelocity = ClimbTuning.UNSTABLE_SLIDE_SPEED;
        }

        double movementVelocity = Math.max(nextVelocity, -ClimbTuning.MAX_BRAKING_MOVE_PER_TICK);
        double lateralSpeed = Math.abs(movementVelocity) * ClimbTuning.BRAKING_LATERAL_SPEED_FACTOR;
        if (state.motion() == AnchorMotion.BRAKING
                && state.committedBrakeDirection().lengthSqr() < ClimbTuning.DIRECTION_EPSILON_SQR) {
            Vec3 requestedDirection = lateralSlideDirection(player, state);
            if (requestedDirection.lengthSqr() >= ClimbTuning.DIRECTION_EPSILON_SQR) {
                state = state.withCommittedBrakeDirection(requestedDirection.normalize());
            }
        }
        Vec3 lateralMovement = state.committedBrakeDirection().lengthSqr() >= ClimbTuning.DIRECTION_EPSILON_SQR
                ? state.committedBrakeDirection().scale(lateralSpeed)
                : lateralSlideDirection(player, state).scale(lateralSpeed);
        Vec3 nextTarget = state.targetPosition().add(lateralMovement).add(0.0D, movementVelocity, 0.0D);
        Vec3 displacement = nextTarget.subtract(player.position());
        if (!player.level().noCollision(player, player.getBoundingBox().move(displacement))) {
            if (!player.level().noCollision(
                    player,
                    player.getBoundingBox().move(0.0D, movementVelocity, 0.0D)
            )) {
                return null;
            }

            return state.withMotion(state.targetPosition(), AnchorMotion.FIXED, 0.0D);
        }

        BlockPos nextAnchorBlock = BlockPos.containing(nextTarget.add(state.contactOffset()));
        BlockState nextBlockState = player.level().getBlockState(nextAnchorBlock);
        if (!AnchorGeometry.hasValidAnchorFace(player, nextBlockState, nextAnchorBlock, state.anchorFace())) {
            return null;
        }

        AnchorSurface nextSurface = AnchorSurfaceResolver.resolve(player, nextAnchorBlock, nextBlockState);
        if (state.surface() == AnchorSurface.UNSTABLE && nextSurface != AnchorSurface.UNSTABLE) {
            nextMotion = AnchorMotion.FIXED;
            nextVelocity = 0.0D;
        }

        ServerClimbState next = state.withSlide(
                nextAnchorBlock,
                nextSurface,
                nextTarget,
                nextMotion,
                nextVelocity
        );
        if (state.motion() == AnchorMotion.BRAKING) {
            double brakingDistance = state.brakingDistance() + Math.abs(movementVelocity);
            int crossedBlocks = (int) Math.floor(brakingDistance) - state.chargedBrakingBlocks();
            next = next.withBrakingProgress(brakingDistance, state.chargedBrakingBlocks());
            if (crossedBlocks > 0) {
                next = applyBrakingWear(player, next, crossedBlocks);
                if (next == null) {
                    return null;
                }
                next = next.withBrakingProgress(brakingDistance, (int) Math.floor(brakingDistance));
            }
        }
        if (!state.anchorBlock().equals(nextAnchorBlock)) {
            AnchorVisualService.clearAnchorVisuals(player, state);
            AnchorVisualService.showCracks(player.serverLevel(), next);
        }
        return next;
    }

    private static boolean hasBrakingSupport(ServerPlayer player, ServerClimbState state) {
        UUID supportToolId = state.brakingSupportToolId();
        return supportToolId != null && ToolLocator.findEquipped(player, supportToolId) != ItemStack.EMPTY;
    }

    private static ServerClimbState applyBrakingWear(
            ServerPlayer player,
            ServerClimbState state,
            int crossedBlocks
    ) {
        if (!ToolWearService.damageEquipped(
                player,
                state.toolId(),
                ToolWearReason.BRAKING_BLOCK,
                crossedBlocks
        )) {
            return null;
        }

        UUID supportToolId = state.brakingSupportToolId();
        if (supportToolId == null || ToolWearService.damageEquipped(
                player,
                supportToolId,
                ToolWearReason.BRAKING_BLOCK,
                crossedBlocks
        )) {
            return state;
        }
        return state.withoutBrakingSupport();
    }

    private static Vec3 lateralSlideDirection(ServerPlayer player, ServerClimbState state) {
        Vec3 look = player.getLookAngle();
        Vec3 forward = new Vec3(look.x, 0.0D, look.z);
        if (forward.lengthSqr() < ClimbTuning.DIRECTION_EPSILON_SQR) {
            return Vec3.ZERO;
        }
        forward = forward.normalize();
        Vec3 right = new Vec3(-forward.z, 0.0D, forward.x);
        Vec3 input = forward.scale(state.lateralForward()).add(right.scale(-state.lateralStrafe()));
        if (input.lengthSqr() > 1.0D) {
            input = input.normalize();
        }

        return switch (state.anchorFace().getAxis()) {
            case X -> new Vec3(0.0D, 0.0D, input.z);
            case Z -> new Vec3(input.x, 0.0D, 0.0D);
            case Y -> Vec3.ZERO;
        };
    }
}
