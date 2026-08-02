package dev.maicra.pickclimber.climb;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;

public record ClientClimbState(
        Vec3 targetPosition,
        InteractionHand activeHand,
        boolean restoreNoGravity,
        boolean restoreFlying,
        long lastSyncGameTime
) {
}
