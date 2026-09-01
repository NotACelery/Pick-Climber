package dev.maicra.pickclimber.climb;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

public final class AnchorInteractionService {
    private AnchorInteractionService() {
    }

    public static AnchorUseDecision forcedAnchor(Player player, BlockHitResult hit) {
        if (!ClimbRuntimeGate.interactionsEnabled(player)) {
            return AnchorUseDecision.pass();
        }

        AnchorEvaluation evaluation = AnchorEvaluator.evaluate(player, hit);
        InteractionHand preferred = evaluation.preferredHand();
        if (preferred == null || ClimbingHandSelector.preservesNormalBlockUse(player, hit)) {
            return AnchorUseDecision.pass();
        }
        return AnchorUseDecision.consume(preferred);
    }

    public static AnchorUseDecision afterBlockUse(
            Player player,
            InteractionHand hand,
            BlockHitResult hit
    ) {
        if (!ClimbRuntimeGate.interactionsEnabled(player)) {
            return AnchorUseDecision.pass();
        }

        AnchorEvaluation evaluation = AnchorEvaluator.evaluate(player, hit);
        if (hand == InteractionHand.MAIN_HAND
                && evaluation.forHand(InteractionHand.OFF_HAND).canAnchor()) {
            return AnchorUseDecision.pass();
        }
        if (evaluation.forHand(hand).canAnchor()) {
            return AnchorUseDecision.consume(hand);
        }
        if (!player.level().isClientSide() || hand != InteractionHand.MAIN_HAND) {
            return AnchorUseDecision.pass();
        }

        Component feedback = AnchorFeedbackResolver.failureMessage(player, hit, evaluation);
        return feedback == null ? AnchorUseDecision.pass() : AnchorUseDecision.feedback(feedback);
    }
}
