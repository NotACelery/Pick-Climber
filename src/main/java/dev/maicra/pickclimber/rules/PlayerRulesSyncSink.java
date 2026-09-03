package dev.maicra.pickclimber.rules;

import net.minecraft.server.level.ServerPlayer;

public interface PlayerRulesSyncSink {
    void sendToPlayer(ServerPlayer player, PlayerRulesSnapshot snapshot);
}
