package dev.maicra.pickclimber.climb;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

final class AnchorSurfaceResolver {
    private AnchorSurfaceResolver() {
    }

    static AnchorSurface resolve(Level level, BlockPos position, BlockState state) {
        return AnchorSurfaceClassifier.classify(state);
    }
}
