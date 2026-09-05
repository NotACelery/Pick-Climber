package dev.maicra.pickclimber.rules;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

import dev.maicra.pickclimber.climb.AnchorSurface;
import dev.maicra.pickclimber.climb.ClimbRulesPolicy;
import dev.maicra.pickclimber.climb.StructuralAnchorSafety;

public final class ClimbingRulesIntegration implements ClimbRulesPolicy {
    public static final ClimbingRulesIntegration INSTANCE = new ClimbingRulesIntegration();

    private ClimbingRulesIntegration() {
    }

    @Override
    public AnchorSurface resolveSurface(
            Player player,
            BlockPos position,
            BlockState state,
            AnchorSurface baseline
    ) {
        ClimbingRulesRuntimeView rules = EffectiveClimbingRulesService.resolve(player);
        if (!rules.active()) {
            return baseline;
        }
        if (!StructuralAnchorSafety.isStructurallyAnchorable(player.level(), position, state)) {
            return AnchorSurface.UNCLIMBABLE;
        }

        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        SurfaceClassification classification = rules.classificationOverride(blockId).orElse(null);
        if (classification != null) {
            return map(classification);
        }
        return rules.unlistedPolicy() == UnlistedPolicy.UNCLIMBABLE
                ? AnchorSurface.UNCLIMBABLE
                : baseline;
    }

    @Override
    public int pickaxeWear(Player player) {
        ClimbingRulesRuntimeView rules = EffectiveClimbingRulesService.resolve(player);
        return rules.active() ? rules.pickaxeWear() : -1;
    }

    private static AnchorSurface map(SurfaceClassification classification) {
        return switch (classification) {
            case STABLE -> AnchorSurface.STABLE;
            case UNSTABLE -> AnchorSurface.UNSTABLE;
            case UNCLIMBABLE -> AnchorSurface.UNCLIMBABLE;
        };
    }
}
