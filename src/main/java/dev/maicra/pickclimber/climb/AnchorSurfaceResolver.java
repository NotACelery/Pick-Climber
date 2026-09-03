package dev.maicra.pickclimber.climb;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

final class AnchorSurfaceResolver {
    private AnchorSurfaceResolver() {
    }

    static AnchorSurface resolve(Player player, BlockPos position, BlockState state) {
        AnchorSurface baseline = AnchorSurfaceClassifier.classify(state);
        return ClimbRulesBridge.resolveSurface(player, position, state, baseline);
    }
}
