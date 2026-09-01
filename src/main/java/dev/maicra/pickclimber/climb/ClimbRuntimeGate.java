package dev.maicra.pickclimber.climb;

import net.minecraft.world.entity.player.Player;

public final class ClimbRuntimeGate {
    private static final ClimbRuntimePolicy DEFAULT_POLICY = player -> true;
    private static ClimbRuntimePolicy policy = DEFAULT_POLICY;

    private ClimbRuntimeGate() {
    }

    public static boolean interactionsEnabled(Player player) {
        return policy.interactionsEnabled(player);
    }

    public static void installPolicy(ClimbRuntimePolicy nextPolicy) {
        policy = nextPolicy == null ? DEFAULT_POLICY : nextPolicy;
    }

    public static void resetPolicy() {
        policy = DEFAULT_POLICY;
    }
}
