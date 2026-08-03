package dev.maicra.pickclimber.climb;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public record ClientClimbState(
        Vec3 targetPosition,
        BlockPos anchorBlock,
        int crackId,
        InteractionHand activeHand,
        UUID toolId,
        boolean restoreNoGravity,
        boolean restoreFlying,
        long lastSyncGameTime,
        long poseStartedGameTime,
        boolean ceilingAnchor
) {
}
