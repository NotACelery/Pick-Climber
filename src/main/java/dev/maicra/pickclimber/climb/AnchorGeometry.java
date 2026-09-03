package dev.maicra.pickclimber.climb;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import static dev.maicra.pickclimber.climb.ClimbTuning.MAX_ANCHOR_MOVE_SQR;
import static dev.maicra.pickclimber.climb.ClimbTuning.WALL_TARGET_COLLISION_STEPS;

final class AnchorGeometry {
    private AnchorGeometry() {
    }

    static boolean hasValidAnchorFace(
            Player player,
            BlockState state,
            BlockPos position,
            Direction face
    ) {
        AnchorSurface surface = AnchorSurfaceResolver.resolve(player, position, state);
        if (state.isAir() || surface == AnchorSurface.UNCLIMBABLE) {
            return false;
        }

        return state.isFaceSturdy(player.level(), position, face)
                || surface == AnchorSurface.UNSTABLE;
    }

    static boolean hasValidCeilingAnchor(
            Player player,
            BlockState state,
            BlockPos position,
            ItemStack tool
    ) {
        AnchorSurface surface = AnchorSurfaceResolver.resolve(player, position, state);
        if (!ModEnchantments.hasStrongGrip(player.level(), tool)
                || surface == AnchorSurface.UNCLIMBABLE
                || surface == AnchorSurface.UNSTABLE
                && !ModEnchantments.hasSturdyLatch(player.level(), tool)) {
            return false;
        }
        return state.isFaceSturdy(player.level(), position, Direction.DOWN)
                || surface == AnchorSurface.UNSTABLE;
    }

    static Vec3 currentAttachmentTarget(Player player) {
        if (player.level().isClientSide()) {
            ClientClimbState state = ClimbStateStore.client(player.getUUID());
            return state == null ? player.position() : state.targetPosition();
        }
        ServerClimbState state = ClimbStateStore.server(player.getUUID());
        return state == null ? player.position() : state.targetPosition();
    }

    static Vec3 calculateTargetPosition(Player player, BlockHitResult hit) {
        BlockPos block = hit.getBlockPos();
        Direction face = hit.getDirection();
        Vec3 location = hit.getLocation();
        double wallOffset = player.getBbWidth() * 0.5D + ClimbTuning.ANCHOR_WALL_CLEARANCE;

        double targetX = location.x;
        double targetZ = location.z;

        switch (face) {
            case EAST -> {
                targetX = block.getX() + 1.0D + wallOffset;
                targetZ = Mth.clamp(location.z, block.getZ() + ClimbTuning.ANCHOR_FACE_EDGE_INSET,
                        block.getZ() + ClimbTuning.ANCHOR_FACE_EDGE_MAX);
            }
            case WEST -> {
                targetX = block.getX() - wallOffset;
                targetZ = Mth.clamp(location.z, block.getZ() + ClimbTuning.ANCHOR_FACE_EDGE_INSET,
                        block.getZ() + ClimbTuning.ANCHOR_FACE_EDGE_MAX);
            }
            case SOUTH -> {
                targetX = Mth.clamp(location.x, block.getX() + ClimbTuning.ANCHOR_FACE_EDGE_INSET,
                        block.getX() + ClimbTuning.ANCHOR_FACE_EDGE_MAX);
                targetZ = block.getZ() + 1.0D + wallOffset;
            }
            case NORTH -> {
                targetX = Mth.clamp(location.x, block.getX() + ClimbTuning.ANCHOR_FACE_EDGE_INSET,
                        block.getX() + ClimbTuning.ANCHOR_FACE_EDGE_MAX);
                targetZ = block.getZ() - wallOffset;
            }
            case DOWN -> {
                targetX = Mth.clamp(location.x, block.getX() + ClimbTuning.ANCHOR_FACE_EDGE_INSET,
                        block.getX() + ClimbTuning.ANCHOR_FACE_EDGE_MAX);
                targetZ = Mth.clamp(location.z, block.getZ() + ClimbTuning.ANCHOR_FACE_EDGE_INSET,
                        block.getZ() + ClimbTuning.ANCHOR_FACE_EDGE_MAX);
                return new Vec3(
                        targetX,
                        block.getY() - player.getBbHeight() - ClimbTuning.ANCHOR_WALL_CLEARANCE,
                        targetZ
                );
            }
            default -> {
            }
        }

        double targetY = location.y - player.getBbHeight() * ClimbTuning.WALL_TARGET_HEIGHT_FACTOR;
        return new Vec3(targetX, targetY, targetZ);
    }

    static Vec3 resolveCollisionSafeTargetPosition(Player player, BlockHitResult hit) {
        Vec3 idealTarget = calculateTargetPosition(player, hit);
        if (isTargetPositionFree(player, idealTarget)) {
            return idealTarget;
        }

        if (hit.getDirection().getAxis() == Direction.Axis.Y) {
            return null;
        }

        double safeReferenceY = currentAttachmentTarget(player).y;
        double verticalCorrection = safeReferenceY - idealTarget.y;
        if (Math.abs(verticalCorrection) > player.getBbHeight() * ClimbTuning.MAX_VERTICAL_TARGET_CORRECTION_FACTOR) {
            return null;
        }

        for (int step = 1; step <= WALL_TARGET_COLLISION_STEPS; step++) {
            double progress = (double) step / WALL_TARGET_COLLISION_STEPS;
            Vec3 candidate = new Vec3(
                    idealTarget.x,
                    Mth.lerp(progress, idealTarget.y, safeReferenceY),
                    idealTarget.z
            );
            if (isTargetPositionFree(player, candidate)) {
                return candidate;
            }
        }
        return null;
    }

    static boolean isWithinAnchorMoveRange(Vec3 movementOrigin, Vec3 target) {
        return movementOrigin.distanceToSqr(target) <= MAX_ANCHOR_MOVE_SQR;
    }

    private static boolean isTargetPositionFree(Player player, Vec3 target) {
        Vec3 displacement = target.subtract(player.position());
        return player.level().noCollision(player, player.getBoundingBox().move(displacement));
    }
}
