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

    /**
     * Returns the configured flat durability loss for standard climbing interactions.
     * A negative value means no climbing rules override is active and vanilla Pick Climber tuning applies.
     */
    default int pickaxeWear(Player player) {
        return -1;
    }

}
