package dev.maicra.pickclimber.rules;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public interface ClimbingRulesSyncSink {
    void sendToPlayer(ServerPlayer player, WorldRulesSnapshot snapshot);

    void broadcast(MinecraftServer server, WorldRulesSnapshot snapshot);
}
