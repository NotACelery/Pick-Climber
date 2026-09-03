package dev.maicra.pickclimber.rules;

import net.minecraft.server.level.ServerPlayer;

@FunctionalInterface
public interface TemporaryRuleBookSyncSink {
    void send(ServerPlayer player, long expiresAtGameTime);
}
