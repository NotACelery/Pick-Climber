package dev.maicra.pickclimber.rules.network;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import dev.maicra.pickclimber.rules.ClimbingRulesSyncSink;
import dev.maicra.pickclimber.rules.WorldRulesSnapshot;

public final class NeoForgeClimbingRulesSyncSink implements ClimbingRulesSyncSink {
    public static final NeoForgeClimbingRulesSyncSink INSTANCE = new NeoForgeClimbingRulesSyncSink();

    private NeoForgeClimbingRulesSyncSink() {
    }

    @Override
    public void sendToPlayer(ServerPlayer player, WorldRulesSnapshot snapshot) {
        PacketDistributor.sendToPlayer(player, WorldRulesStatePayload.from(snapshot));
    }

    @Override
    public void broadcast(MinecraftServer server, WorldRulesSnapshot snapshot) {
        PacketDistributor.sendToAllPlayers(WorldRulesStatePayload.from(snapshot));
    }
}
