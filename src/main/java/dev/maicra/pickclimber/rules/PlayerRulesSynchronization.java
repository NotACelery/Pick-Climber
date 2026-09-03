package dev.maicra.pickclimber.rules;

import net.minecraft.server.level.ServerPlayer;

public final class PlayerRulesSynchronization {
    private static final PlayerRulesSyncSink NO_OP = (player, snapshot) -> {
    };

    private static PlayerRulesSyncSink sink = NO_OP;

    private PlayerRulesSynchronization() {
    }

    public static void install(PlayerRulesSyncSink nextSink) {
        sink = nextSink == null ? NO_OP : nextSink;
    }

    public static void sendToPlayer(ServerPlayer player, PlayerRulesSnapshot snapshot) {
        sink.sendToPlayer(player, snapshot);
    }
}
