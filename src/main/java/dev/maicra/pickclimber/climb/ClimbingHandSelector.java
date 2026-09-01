package dev.maicra.pickclimber.climb;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class ClimbingHandSelector {
    private ClimbingHandSelector() {
    }

    public static InteractionHand preferred(Player player, BlockHitResult hit) {
        AnchorEvaluation evaluation = AnchorEvaluator.evaluate(player, hit);
        InteractionHand preferred = evaluation.preferredHand();
        return preferred == null || preservesNormalBlockUse(player, hit) ? null : preferred;
    }

    public static boolean preservesNormalBlockUse(Player player, BlockHitResult hit) {
        if (player.isSecondaryUseActive()) {
            return false;
        }

        BlockState state = player.level().getBlockState(hit.getBlockPos());
        return BlockInteractionClassifier.handlesNormalBlockUse(
                state,
                player.level(),
                hit.getBlockPos()
        );
    }
}
