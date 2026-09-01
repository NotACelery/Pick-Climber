package dev.maicra.pickclimber.climb;

import net.minecraft.world.entity.player.Player;

public final class ClimbPresentationGate {
    private static final ClimbPresentationPolicy DEFAULT_POLICY = new ClimbPresentationPolicy() {
        @Override
        public AnchorIndicatorStatus filterIndicator(Player player, AnchorIndicatorStatus status) {
            return status;
        }

        @Override
        public boolean showFailureText(Player player) {
            return true;
        }
    };

    private static ClimbPresentationPolicy policy = DEFAULT_POLICY;

    private ClimbPresentationGate() {
    }

    public static AnchorIndicatorStatus filterIndicator(Player player, AnchorIndicatorStatus status) {
        return policy.filterIndicator(player, status);
    }

    public static boolean showFailureText(Player player) {
        return policy.showFailureText(player);
    }

    public static void installPolicy(ClimbPresentationPolicy nextPolicy) {
        policy = nextPolicy == null ? DEFAULT_POLICY : nextPolicy;
    }

    public static void resetPolicy() {
        policy = DEFAULT_POLICY;
    }
}
