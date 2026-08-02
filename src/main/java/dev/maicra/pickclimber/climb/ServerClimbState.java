package dev.maicra.pickclimber.climb;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public record ServerClimbState(
        ResourceKey<Level> anchorDimension,
        BlockPos anchorBlock,
        Direction anchorFace,
        Vec3 targetPosition,
        InteractionHand activeHand,
        UUID toolId,
        int crackId,
        boolean restoreNoGravity,
        boolean restoreFlying,
        long attachedAtGameTime,
        AnchorSurface surface,
        AnchorMotion motion,
        double slideVelocity,
        int cooldownTicks,
        float lateralForward,
        float lateralStrafe,
        Vec3 contactOffset,
        Vec3 committedBrakeDirection,
        boolean reinforcedLatch,
        UUID brakingSupportToolId,
        double brakingDistance,
        int chargedBrakingBlocks
) {
    public ServerClimbState withActiveHand(InteractionHand hand) {
        return new ServerClimbState(
                anchorDimension,
                anchorBlock,
                anchorFace,
                targetPosition,
                hand,
                toolId,
                crackId,
                restoreNoGravity,
                restoreFlying,
                attachedAtGameTime,
                surface,
                motion,
                slideVelocity,
                cooldownTicks,
                lateralForward,
                lateralStrafe,
                contactOffset,
                committedBrakeDirection,
                reinforcedLatch,
                brakingSupportToolId,
                brakingDistance,
                chargedBrakingBlocks
        );
    }

    public ServerClimbState withMotion(Vec3 target, AnchorMotion nextMotion, double nextSlideVelocity) {
        return withSlide(anchorBlock, surface, target, nextMotion, nextSlideVelocity);
    }

    public ServerClimbState withSlide(
            BlockPos nextAnchorBlock,
            AnchorSurface nextSurface,
            Vec3 target,
            AnchorMotion nextMotion,
            double nextSlideVelocity
    ) {
        return new ServerClimbState(
                anchorDimension,
                nextAnchorBlock,
                anchorFace,
                target,
                activeHand,
                toolId,
                crackId,
                restoreNoGravity,
                restoreFlying,
                attachedAtGameTime,
                nextSurface,
                nextMotion,
                nextSlideVelocity,
                cooldownTicks,
                lateralForward,
                lateralStrafe,
                contactOffset,
                committedBrakeDirection,
                reinforcedLatch,
                brakingSupportToolId,
                brakingDistance,
                chargedBrakingBlocks
        );
    }

    public ServerClimbState withSlideInput(float nextForward, float nextStrafe) {
        return new ServerClimbState(
                anchorDimension,
                anchorBlock,
                anchorFace,
                targetPosition,
                activeHand,
                toolId,
                crackId,
                restoreNoGravity,
                restoreFlying,
                attachedAtGameTime,
                surface,
                motion,
                slideVelocity,
                cooldownTicks,
                nextForward,
                nextStrafe,
                contactOffset,
                committedBrakeDirection,
                reinforcedLatch,
                brakingSupportToolId,
                brakingDistance,
                chargedBrakingBlocks
        );
    }

    public ServerClimbState withCommittedBrakeDirection(Vec3 direction) {
        return new ServerClimbState(
                anchorDimension, anchorBlock, anchorFace, targetPosition, activeHand,
                toolId, crackId, restoreNoGravity, restoreFlying, attachedAtGameTime,
                surface, motion, slideVelocity, cooldownTicks, lateralForward, lateralStrafe,
                contactOffset, direction, reinforcedLatch, brakingSupportToolId,
                brakingDistance, chargedBrakingBlocks
        );
    }

    public ServerClimbState withBrakingProgress(double distance, int chargedBlocks) {
        return new ServerClimbState(
                anchorDimension, anchorBlock, anchorFace, targetPosition, activeHand,
                toolId, crackId, restoreNoGravity, restoreFlying, attachedAtGameTime,
                surface, motion, slideVelocity, cooldownTicks, lateralForward, lateralStrafe,
                contactOffset, committedBrakeDirection, reinforcedLatch, brakingSupportToolId,
                distance, chargedBlocks
        );
    }

    public ServerClimbState withoutBrakingSupport() {
        return new ServerClimbState(
                anchorDimension, anchorBlock, anchorFace, targetPosition, activeHand,
                toolId, crackId, restoreNoGravity, restoreFlying, attachedAtGameTime,
                surface, motion, slideVelocity, cooldownTicks, lateralForward, lateralStrafe,
                contactOffset, committedBrakeDirection, reinforcedLatch, null,
                brakingDistance, chargedBrakingBlocks
        );
    }
}
