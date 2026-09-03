package dev.maicra.pickclimber.rules;

import net.minecraft.server.level.ServerPlayer;

public final class TemporaryRuleBookSynchronization {
    private static final TemporaryRuleBookSyncSink NO_OP = (player, expiresAtGameTime) -> {
    };
    private static TemporaryRuleBookSyncSink sink = NO_OP;

    private TemporaryRuleBookSynchronization() {
    }

    public static void install(TemporaryRuleBookSyncSink replacement) {
        sink = replacement == null ? NO_OP : replacement;
    }

    public static void send(ServerPlayer player, long expiresAtGameTime) {
        sink.send(player, Math.max(0L, expiresAtGameTime));
    }
}
