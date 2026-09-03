package dev.maicra.pickclimber.climb;

import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import static dev.maicra.pickclimber.climb.ClimbTuning.MAX_HIT_DISTANCE_SQR;

final class AnchorEvaluator {
    private AnchorEvaluator() {
    }

    static AnchorEvaluation evaluate(Player player, BlockHitResult hit) {
        BlockState state = player.level().getBlockState(hit.getBlockPos());
        AnchorSurface surface = AnchorSurfaceResolver.resolve(player, hit.getBlockPos(), state);
        boolean validAnchorFace = AnchorGeometry.hasValidAnchorFace(
                player,
                state,
                hit.getBlockPos(),
                hit.getDirection()
        );
        Vec3 idealTarget = AnchorGeometry.calculateTargetPosition(player, hit);
        Vec3 resolvedTarget = validAnchorFace
                ? AnchorGeometry.resolveCollisionSafeTargetPosition(player, hit)
                : null;
        Vec3 movementOrigin = AnchorGeometry.currentAttachmentTarget(player);
        boolean ceilingAttempt = hit.getDirection() == Direction.DOWN;

        return new AnchorEvaluation(
                state,
                surface,
                validAnchorFace,
                ceilingAttempt,
                idealTarget,
                resolvedTarget,
                movementOrigin,
                evaluateHand(
                        player,
                        InteractionHand.OFF_HAND,
                        hit,
                        surface,
                        validAnchorFace,
                        ceilingAttempt,
                        resolvedTarget,
                        movementOrigin
                ),
                evaluateHand(
                        player,
                        InteractionHand.MAIN_HAND,
                        hit,
                        surface,
                        validAnchorFace,
                        ceilingAttempt,
                        resolvedTarget,
                        movementOrigin
                )
        );
    }

    private static AnchorHandEvaluation evaluateHand(
            Player player,
            InteractionHand hand,
            BlockHitResult hit,
            AnchorSurface surface,
            boolean validAnchorFace,
            boolean ceilingAttempt,
            Vec3 resolvedTarget,
            Vec3 movementOrigin
    ) {
        ItemStack stack = player.getItemInHand(hand);
        boolean climbingTool = ClimbingToolClassifier.isClimbingTool(stack);
        boolean strongGrip = climbingTool && ModEnchantments.hasStrongGrip(player.level(), stack);
        boolean sturdyLatch = climbingTool && ModEnchantments.hasSturdyLatch(player.level(), stack);
        boolean duplicatedIdentity = climbingTool && hasDuplicatedActiveIdentity(player, hand, stack);
        boolean occupied = climbingTool
                && !duplicatedIdentity
                && ClimbSessionView.isActiveTool(player, stack)
                && ClimbSessionView.activeHand(player) == hand;
        boolean coolingDown = climbingTool
                && !duplicatedIdentity
                && AnchorCooldownService.isCoolingDown(stack, player.level().getGameTime());

        if (!player.isAlive() || player.isSpectator() || player.isFallFlying()) {
            return failure(
                    hand,
                    AnchorFailureReason.PLAYER_UNAVAILABLE,
                    duplicatedIdentity,
                    occupied,
                    coolingDown,
                    strongGrip,
                    sturdyLatch
            );
        }
        if (hit.getDirection() == Direction.UP) {
            return failure(
                    hand,
                    AnchorFailureReason.INVALID_FACE,
                    duplicatedIdentity,
                    occupied,
                    coolingDown,
                    strongGrip,
                    sturdyLatch
            );
        }
        if (!climbingTool) {
            return failure(
                    hand,
                    AnchorFailureReason.NOT_CLIMBING_TOOL,
                    false,
                    false,
                    false,
                    false,
                    false
            );
        }
        if (ceilingAttempt && !strongGrip) {
            return failure(
                    hand,
                    AnchorFailureReason.REQUIRES_STRONG_GRIP,
                    duplicatedIdentity,
                    occupied,
                    coolingDown,
                    false,
                    sturdyLatch
            );
        }
        if (occupied) {
            return failure(
                    hand,
                    AnchorFailureReason.TOOL_OCCUPIED,
                    duplicatedIdentity,
                    true,
                    coolingDown,
                    strongGrip,
                    sturdyLatch
            );
        }
        if (coolingDown) {
            return failure(
                    hand,
                    AnchorFailureReason.COOLDOWN,
                    duplicatedIdentity,
                    false,
                    true,
                    strongGrip,
                    sturdyLatch
            );
        }
        if (player.getEyePosition().distanceToSqr(hit.getLocation()) > MAX_HIT_DISTANCE_SQR) {
            return failure(
                    hand,
                    AnchorFailureReason.TOO_FAR_FROM_EYES,
                    duplicatedIdentity,
                    false,
                    false,
                    strongGrip,
                    sturdyLatch
            );
        }
        if (!validAnchorFace) {
            AnchorFailureReason reason = surface == AnchorSurface.UNCLIMBABLE
                    ? AnchorFailureReason.UNCLIMBABLE
                    : AnchorFailureReason.INVALID_ANCHOR_FACE;
            return failure(
                    hand,
                    reason,
                    duplicatedIdentity,
                    false,
                    false,
                    strongGrip,
                    sturdyLatch
            );
        }
        if (ceilingAttempt && surface == AnchorSurface.UNSTABLE && !sturdyLatch) {
            return failure(
                    hand,
                    AnchorFailureReason.REQUIRES_STURDY_LATCH,
                    duplicatedIdentity,
                    false,
                    false,
                    strongGrip,
                    false
            );
        }
        if (resolvedTarget == null) {
            return failure(
                    hand,
                    AnchorFailureReason.NO_SPACE,
                    duplicatedIdentity,
                    false,
                    false,
                    strongGrip,
                    sturdyLatch
            );
        }
        if (!AnchorGeometry.isWithinAnchorMoveRange(movementOrigin, resolvedTarget)) {
            return failure(
                    hand,
                    AnchorFailureReason.OUT_OF_RANGE,
                    duplicatedIdentity,
                    false,
                    false,
                    strongGrip,
                    sturdyLatch
            );
        }

        return AnchorHandEvaluation.success(
                hand,
                resolvedTarget,
                duplicatedIdentity,
                strongGrip,
                sturdyLatch
        );
    }

    private static AnchorHandEvaluation failure(
            InteractionHand hand,
            AnchorFailureReason reason,
            boolean duplicatedIdentity,
            boolean occupied,
            boolean coolingDown,
            boolean strongGrip,
            boolean sturdyLatch
    ) {
        return AnchorHandEvaluation.failure(
                hand,
                reason,
                duplicatedIdentity,
                occupied,
                coolingDown,
                strongGrip,
                sturdyLatch
        );
    }

    private static boolean hasDuplicatedActiveIdentity(
            Player player,
            InteractionHand candidateHand,
            ItemStack candidate
    ) {
        InteractionHand currentActiveHand = ClimbSessionView.activeHand(player);
        if (currentActiveHand == null
                || currentActiveHand == candidateHand
                || !ClimbSessionView.isActiveTool(player, candidate)) {
            return false;
        }
        return ToolIdentity.get(candidate)
                .map(id -> ToolIdentity.matches(player.getItemInHand(currentActiveHand), id))
                .orElse(false);
    }
}
