package dev.maicra.pickclimber.climb;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

public final class ClimbRulesBridge {
    private static final ClimbRulesPolicy DEFAULT_POLICY = new ClimbRulesPolicy() {
    };

    private static ClimbRulesPolicy policy = DEFAULT_POLICY;

    private ClimbRulesBridge() {
    }

    public static void install(ClimbRulesPolicy nextPolicy) {
        policy = nextPolicy == null ? DEFAULT_POLICY : nextPolicy;
    }

    static AnchorSurface resolveSurface(
            Player player,
            BlockPos position,
            BlockState state,
            AnchorSurface baseline
    ) {
        AnchorSurface resolved = policy.resolveSurface(player, position, state, baseline);
        return resolved == null ? baseline : resolved;
    }

    static int durabilityMultiplierPercent(Player player) {
        return Math.max(0, Math.min(500, policy.durabilityMultiplierPercent(player)));
    }

    static long durabilityPolicyRevision(Player player) {
        return policy.durabilityPolicyRevision(player);
    }
}
