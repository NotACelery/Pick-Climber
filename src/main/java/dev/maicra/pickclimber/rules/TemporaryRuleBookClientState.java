package dev.maicra.pickclimber.rules;

public final class TemporaryRuleBookClientState {
    private static volatile long expiresAtGameTime;

    private TemporaryRuleBookClientState() {
    }

    public static long expiresAtGameTime() {
        return expiresAtGameTime;
    }

    public static void apply(long expiresAt) {
        expiresAtGameTime = Math.max(0L, expiresAt);
    }

    public static void clear() {
        expiresAtGameTime = 0L;
    }
}
