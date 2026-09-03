package dev.maicra.pickclimber.rules;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class ClimbingRulesSynchronization {
    private static final ClimbingRulesSyncSink NO_OP = new ClimbingRulesSyncSink() {
        @Override
        public void sendToPlayer(ServerPlayer player, WorldRulesSnapshot snapshot) {
        }

        @Override
        public void broadcast(MinecraftServer server, WorldRulesSnapshot snapshot) {
        }
    };

    private static ClimbingRulesSyncSink sink = NO_OP;

    private ClimbingRulesSynchronization() {
    }

    public static void install(ClimbingRulesSyncSink nextSink) {
        sink = nextSink == null ? NO_OP : nextSink;
    }

    public static void sendToPlayer(ServerPlayer player, WorldRulesSnapshot snapshot) {
        sink.sendToPlayer(player, snapshot);
    }

    static void broadcast(MinecraftServer server, WorldRulesSnapshot snapshot) {
        sink.broadcast(server, snapshot);
    }
}
