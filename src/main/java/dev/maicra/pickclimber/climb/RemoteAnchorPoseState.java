package dev.maicra.pickclimber.climb;

import net.minecraft.world.InteractionHand;

public record RemoteAnchorPoseState(
        InteractionHand activeHand,
        long lastSyncGameTime
) {
}
