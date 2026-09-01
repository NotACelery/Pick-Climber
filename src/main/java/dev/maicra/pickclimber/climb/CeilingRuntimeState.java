package dev.maicra.pickclimber.climb;

import net.minecraft.world.phys.Vec3;

record CeilingRuntimeState(Vec3 center, Vec3 velocity, Vec3 releaseMomentum) {
    CeilingRuntimeState withSwing(Vec3 nextVelocity, Vec3 nextReleaseMomentum) {
        return new CeilingRuntimeState(center, nextVelocity, nextReleaseMomentum);
    }
}
