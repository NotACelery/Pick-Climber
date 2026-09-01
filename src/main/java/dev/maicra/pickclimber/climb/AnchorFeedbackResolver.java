package dev.maicra.pickclimber.climb;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import static dev.maicra.pickclimber.climb.ClimbTuning.MAX_INDICATOR_DISTANCE_SQR;

final class AnchorFeedbackResolver {
    private AnchorFeedbackResolver() {
    }

    static AnchorIndicatorStatus indicatorStatus(
            Player player,
            BlockHitResult hit,
            AnchorEvaluation evaluation
    ) {
        if (!player.level().isClientSide()
                || !player.isAlive()
                || player.isSpectator()
                || player.isFallFlying()
                || hit.getType() != HitResult.Type.BLOCK
                || hit.getDirection() == Direction.UP
                || ClimbingHandSelector.preservesNormalBlockUse(player, hit)) {
            return AnchorIndicatorStatus.NONE;
        }

        boolean hasClimbingTool = false;
        for (InteractionHand hand : InteractionHand.values()) {
            if (ClimbingToolClassifier.isClimbingTool(player.getItemInHand(hand))) {
                hasClimbingTool = true;
                break;
            }
        }
        if (!hasClimbingTool) {
            return AnchorIndicatorStatus.NONE;
        }

        if (player.getEyePosition().distanceToSqr(hit.getLocation()) > MAX_INDICATOR_DISTANCE_SQR) {
            return AnchorIndicatorStatus.NONE;
        }
        if (evaluation.surface() == AnchorSurface.UNCLIMBABLE) {
            return AnchorIndicatorStatus.UNCLIMBABLE;
        }

        InteractionHand preferredHand = evaluation.preferredHand();
        if (preferredHand != null) {
            AnchorHandEvaluation preferred = evaluation.forHand(preferredHand);
            return evaluation.surface() == AnchorSurface.UNSTABLE && !preferred.sturdyLatch()
                    ? AnchorIndicatorStatus.UNSTABLE
                    : AnchorIndicatorStatus.READY;
        }

        if (!AnchorGeometry.isWithinAnchorMoveRange(evaluation.movementOrigin(), evaluation.idealTarget())) {
            return AnchorIndicatorStatus.OUT_OF_RANGE;
        }
        if (!evaluation.validAnchorFace()) {
            return AnchorIndicatorStatus.OBSTRUCTED;
        }
        if (evaluation.resolvedTarget() == null) {
            return AnchorIndicatorStatus.OBSTRUCTED;
        }
        if (!AnchorGeometry.isWithinAnchorMoveRange(evaluation.movementOrigin(), evaluation.resolvedTarget())) {
            return AnchorIndicatorStatus.OUT_OF_RANGE;
        }

        if (evaluation.ceilingAttempt()) {
            boolean hasUnoccupiedClimbingTool = false;
            boolean hasStrongGrip = false;
            boolean hasRequiredEnchantments = false;

            for (InteractionHand hand : InteractionHand.values()) {
                ItemStack stack = player.getItemInHand(hand);
                if (!ClimbingToolClassifier.isClimbingTool(stack) || evaluation.forHand(hand).occupied()) {
                    continue;
                }
                hasUnoccupiedClimbingTool = true;
                AnchorHandEvaluation handEvaluation = evaluation.forHand(hand);
                if (!handEvaluation.strongGrip()) {
                    continue;
                }
                hasStrongGrip = true;
                if (evaluation.surface() != AnchorSurface.UNSTABLE || handEvaluation.sturdyLatch()) {
                    hasRequiredEnchantments = true;
                }
            }

            if (hasUnoccupiedClimbingTool && !hasStrongGrip) {
                return AnchorIndicatorStatus.REQUIRES_STRONG_GRIP;
            }
            if (hasStrongGrip && !hasRequiredEnchantments) {
                return AnchorIndicatorStatus.REQUIRES_STURDY_LATCH;
            }
        }

        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (!ClimbingToolClassifier.isClimbingTool(stack)) {
                continue;
            }

            AnchorHandEvaluation handEvaluation = evaluation.forHand(hand);
            if (evaluation.ceilingAttempt()
                    && (!handEvaluation.strongGrip()
                    || evaluation.surface() == AnchorSurface.UNSTABLE
                    && !handEvaluation.sturdyLatch())) {
                continue;
            }
            if (handEvaluation.occupied() || handEvaluation.coolingDown()) {
                return AnchorIndicatorStatus.COOLDOWN;
            }
        }

        return AnchorIndicatorStatus.OBSTRUCTED;
    }

    static Component failureMessage(
            Player player,
            BlockHitResult hit,
            AnchorEvaluation evaluation
    ) {
        AnchorIndicatorStatus status = indicatorStatus(player, hit, evaluation);
        return switch (status) {
            case REQUIRES_STRONG_GRIP -> Component.translatable(
                    "message.pickclimber.requires_strong_grip"
            );
            case COOLDOWN -> Component.translatable("message.pickclimber.anchor.cooldown");
            case OUT_OF_RANGE -> Component.translatable("message.pickclimber.anchor.out_of_range");
            case UNCLIMBABLE -> Component.translatable(
                    "message.pickclimber.anchor.blocked_by",
                    evaluation.blockState().getBlock().getName()
            );
            case OBSTRUCTED -> obstructionMessage(evaluation);
            case NONE, READY, UNSTABLE, REQUIRES_STURDY_LATCH -> null;
        };
    }

    private static Component obstructionMessage(AnchorEvaluation evaluation) {
        if (!evaluation.validAnchorFace()) {
            return Component.translatable(
                    "message.pickclimber.anchor.blocked_by",
                    evaluation.blockState().getBlock().getName()
            );
        }
        if (evaluation.resolvedTarget() == null) {
            return Component.translatable("message.pickclimber.anchor.not_enough_space");
        }
        return Component.translatable("message.pickclimber.anchor.obstructed");
    }
}
