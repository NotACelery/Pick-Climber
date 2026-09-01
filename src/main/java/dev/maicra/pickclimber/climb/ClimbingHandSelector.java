package dev.maicra.pickclimber.climb;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class ClimbingHandSelector {
    private ClimbingHandSelector() {
    }

    public static InteractionHand preferred(Player player, BlockHitResult hit) {
        InteractionHand preferred = null;

        if (ClimbManager.canAttemptAnchor(player, InteractionHand.OFF_HAND, hit)) {
            preferred = InteractionHand.OFF_HAND;
        } else if (ClimbManager.canAttemptAnchor(player, InteractionHand.MAIN_HAND, hit)) {
            preferred = InteractionHand.MAIN_HAND;
        }

        if (preferred == null || preservesNormalBlockUse(player, hit)) {
            return null;
        }

        return preferred;
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
