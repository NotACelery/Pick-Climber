package dev.maicra.pickclimber.climb;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public record ServerClimbState(
        BlockPos anchorBlock,
        Direction anchorFace,
        Vec3 targetPosition,
        InteractionHand activeHand,
        UUID toolId,
        int crackId,
        boolean restoreNoGravity,
        boolean restoreFlying,
        long attachedAtGameTime
) {
}
