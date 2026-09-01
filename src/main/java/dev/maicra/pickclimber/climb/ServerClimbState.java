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
        AttachmentRestoreState restoreState,
        long attachedAtGameTime,
        AnchorSurface surface,
        AnchorMotion motion,
        double slideVelocity,
        int cooldownTicks,
        AnchorControlInput input,
        Vec3 contactOffset,
        BrakingRuntimeState braking,
        boolean reinforcedLatch,
        CeilingRuntimeState ceiling
) {
    public ServerClimbState(
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
            int chargedBrakingBlocks,
            Vec3 ceilingCenter,
            Vec3 swingVelocity,
            Vec3 swingReleaseMomentum
    ) {
        this(
                anchorDimension,
                anchorBlock,
                anchorFace,
                targetPosition,
                activeHand,
                toolId,
                crackId,
                new AttachmentRestoreState(restoreNoGravity, restoreFlying),
                attachedAtGameTime,
                surface,
                motion,
                slideVelocity,
                cooldownTicks,
                new AnchorControlInput(lateralForward, lateralStrafe),
                contactOffset,
                new BrakingRuntimeState(
                        committedBrakeDirection,
                        brakingSupportToolId,
                        brakingDistance,
                        chargedBrakingBlocks
                ),
                reinforcedLatch,
                new CeilingRuntimeState(ceilingCenter, swingVelocity, swingReleaseMomentum)
        );
    }

    public boolean restoreNoGravity() {
        return restoreState.noGravity();
    }

    public boolean restoreFlying() {
        return restoreState.flying();
    }

    public float lateralForward() {
        return input.forward();
    }

    public float lateralStrafe() {
        return input.strafe();
    }

    public Vec3 committedBrakeDirection() {
        return braking.committedDirection();
    }

    public UUID brakingSupportToolId() {
        return braking.supportToolId();
    }

    public double brakingDistance() {
        return braking.distance();
    }

    public int chargedBrakingBlocks() {
        return braking.chargedBlocks();
    }

    public Vec3 ceilingCenter() {
        return ceiling.center();
    }

    public Vec3 swingVelocity() {
        return ceiling.velocity();
    }

    public Vec3 swingReleaseMomentum() {
        return ceiling.releaseMomentum();
    }

    public ServerClimbState withActiveHand(InteractionHand hand) {
        return copy(anchorBlock, targetPosition, hand, surface, motion, slideVelocity, input, braking, ceiling);
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
        return copy(
                nextAnchorBlock,
                target,
                activeHand,
                nextSurface,
                nextMotion,
                nextSlideVelocity,
                input,
                braking,
                ceiling
        );
    }

    public ServerClimbState withSlideInput(float nextForward, float nextStrafe) {
        return copy(
                anchorBlock,
                targetPosition,
                activeHand,
                surface,
                motion,
                slideVelocity,
                new AnchorControlInput(nextForward, nextStrafe),
                braking,
                ceiling
        );
    }

    public ServerClimbState withCommittedBrakeDirection(Vec3 direction) {
        return copy(
                anchorBlock,
                targetPosition,
                activeHand,
                surface,
                motion,
                slideVelocity,
                input,
                braking.withCommittedDirection(direction),
                ceiling
        );
    }

    public ServerClimbState withBrakingProgress(double distance, int chargedBlocks) {
        return copy(
                anchorBlock,
                targetPosition,
                activeHand,
                surface,
                motion,
                slideVelocity,
                input,
                braking.withProgress(distance, chargedBlocks),
                ceiling
        );
    }

    public ServerClimbState withoutBrakingSupport() {
        return copy(
                anchorBlock,
                targetPosition,
                activeHand,
                surface,
                motion,
                slideVelocity,
                input,
                braking.withoutSupport(),
                ceiling
        );
    }

    public ServerClimbState withCeilingSwing(Vec3 target, Vec3 velocity, Vec3 releaseMomentum) {
        return copy(
                anchorBlock,
                target,
                activeHand,
                surface,
                motion,
                slideVelocity,
                input,
                braking,
                ceiling.withSwing(velocity, releaseMomentum)
        );
    }

    private ServerClimbState copy(
            BlockPos nextAnchorBlock,
            Vec3 nextTarget,
            InteractionHand nextHand,
            AnchorSurface nextSurface,
            AnchorMotion nextMotion,
            double nextSlideVelocity,
            AnchorControlInput nextInput,
            BrakingRuntimeState nextBraking,
            CeilingRuntimeState nextCeiling
    ) {
        return new ServerClimbState(
                anchorDimension,
                nextAnchorBlock,
                anchorFace,
                nextTarget,
                nextHand,
                toolId,
                crackId,
                restoreState,
                attachedAtGameTime,
                nextSurface,
                nextMotion,
                nextSlideVelocity,
                cooldownTicks,
                nextInput,
                contactOffset,
                nextBraking,
                reinforcedLatch,
                nextCeiling
        );
    }
}
