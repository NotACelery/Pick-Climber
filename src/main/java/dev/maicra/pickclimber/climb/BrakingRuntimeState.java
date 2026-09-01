package dev.maicra.pickclimber.climb;

import net.minecraft.world.phys.Vec3;

import java.util.UUID;

record BrakingRuntimeState(
        Vec3 committedDirection,
        UUID supportToolId,
        double distance,
        int chargedBlocks
) {
    BrakingRuntimeState withCommittedDirection(Vec3 direction) {
        return new BrakingRuntimeState(direction, supportToolId, distance, chargedBlocks);
    }

    BrakingRuntimeState withProgress(double nextDistance, int nextChargedBlocks) {
        return new BrakingRuntimeState(
                committedDirection,
                supportToolId,
                nextDistance,
                nextChargedBlocks
        );
    }

    BrakingRuntimeState withoutSupport() {
        return new BrakingRuntimeState(committedDirection, null, distance, chargedBlocks);
    }
}
