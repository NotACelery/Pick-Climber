package dev.maicra.pickclimber.climb;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public record ClientAnchorSync(
        boolean attached,
        Vec3 target,
        BlockPos anchorBlock,
        int crackId,
        InteractionHand hand,
        UUID toolId,
        boolean restoreNoGravity,
        boolean restoreFlying,
        boolean jumpDetach,
        boolean newAnchor,
        boolean refundCooldown,
        boolean ceilingAnchor,
        int cooldownTicks
) {
}
