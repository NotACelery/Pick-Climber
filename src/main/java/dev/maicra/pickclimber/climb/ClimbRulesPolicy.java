package dev.maicra.pickclimber.climb;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

public interface ClimbRulesPolicy {
    default AnchorSurface resolveSurface(
            Player player,
            BlockPos position,
            BlockState state,
            AnchorSurface baseline
    ) {
        return baseline;
    }

    default int durabilityMultiplierPercent(Player player) {
        return 100;
    }

    default long durabilityPolicyRevision(Player player) {
        return 0L;
    }
}
