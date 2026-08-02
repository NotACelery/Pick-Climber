package dev.maicra.pickclimber.climb;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public record ServerClimbState(
        ResourceKey<Level> anchorDimension,
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
    public ServerClimbState withActiveHand(InteractionHand hand) {
        return new ServerClimbState(
                anchorDimension,
                anchorBlock,
                anchorFace,
                targetPosition,
                hand,
                toolId,
                crackId,
                restoreNoGravity,
                restoreFlying,
                attachedAtGameTime
        );
    }
}
