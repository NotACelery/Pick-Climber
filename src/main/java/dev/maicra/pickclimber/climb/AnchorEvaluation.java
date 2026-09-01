package dev.maicra.pickclimber.climb;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

record AnchorEvaluation(
        BlockState blockState,
        AnchorSurface surface,
        boolean validAnchorFace,
        boolean ceilingAttempt,
        Vec3 idealTarget,
        Vec3 resolvedTarget,
        Vec3 movementOrigin,
        AnchorHandEvaluation offHand,
        AnchorHandEvaluation mainHand
) {
    AnchorHandEvaluation forHand(InteractionHand hand) {
        return hand == InteractionHand.OFF_HAND ? offHand : mainHand;
    }

    InteractionHand preferredHand() {
        if (offHand.canAnchor()) {
            return InteractionHand.OFF_HAND;
        }
        return mainHand.canAnchor() ? InteractionHand.MAIN_HAND : null;
    }
}
