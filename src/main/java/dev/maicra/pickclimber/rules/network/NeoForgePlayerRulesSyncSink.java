package dev.maicra.pickclimber.rules.network;

import dev.maicra.pickclimber.rules.PlayerRulesSnapshot;
import dev.maicra.pickclimber.rules.PlayerRulesSyncSink;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public final class NeoForgePlayerRulesSyncSink implements PlayerRulesSyncSink {
    public static final NeoForgePlayerRulesSyncSink INSTANCE = new NeoForgePlayerRulesSyncSink();

    private NeoForgePlayerRulesSyncSink() {
    }

    @Override
    public void sendToPlayer(ServerPlayer player, PlayerRulesSnapshot snapshot) {
        PacketDistributor.sendToPlayer(player, PlayerRulesStatePayload.from(snapshot));
    }
}
