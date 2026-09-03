package dev.maicra.pickclimber.rules.network;

import dev.maicra.pickclimber.rules.TemporaryRuleBookSyncSink;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public enum NeoForgeTemporaryRuleBookSyncSink implements TemporaryRuleBookSyncSink {
    INSTANCE;

    @Override
    public void send(ServerPlayer player, long expiresAtGameTime) {
        PacketDistributor.sendToPlayer(player, new TemporaryRuleBookStatePayload(expiresAtGameTime));
    }
}
