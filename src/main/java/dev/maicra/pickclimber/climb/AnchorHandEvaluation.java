package dev.maicra.pickclimber.climb;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;

record AnchorHandEvaluation(
        InteractionHand hand,
        boolean canAnchor,
        AnchorFailureReason failureReason,
        Vec3 targetPosition,
        boolean duplicatedActiveIdentity,
        boolean occupied,
        boolean coolingDown,
        boolean strongGrip,
        boolean sturdyLatch
) {
    static AnchorHandEvaluation success(
            InteractionHand hand,
            Vec3 targetPosition,
            boolean duplicatedActiveIdentity,
            boolean strongGrip,
            boolean sturdyLatch
    ) {
        return new AnchorHandEvaluation(
                hand,
                true,
                AnchorFailureReason.NONE,
                targetPosition,
                duplicatedActiveIdentity,
                false,
                false,
                strongGrip,
                sturdyLatch
        );
    }

    static AnchorHandEvaluation failure(
            InteractionHand hand,
            AnchorFailureReason reason,
            boolean duplicatedActiveIdentity,
            boolean occupied,
            boolean coolingDown,
            boolean strongGrip,
            boolean sturdyLatch
    ) {
        return new AnchorHandEvaluation(
                hand,
                false,
                reason,
                null,
                duplicatedActiveIdentity,
                occupied,
                coolingDown,
                strongGrip,
                sturdyLatch
        );
    }
}
