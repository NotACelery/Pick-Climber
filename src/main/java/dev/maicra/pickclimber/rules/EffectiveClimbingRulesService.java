package dev.maicra.pickclimber.rules;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import dev.maicra.pickclimber.rules.persistence.ClimbingRulesSavedData;

public final class EffectiveClimbingRulesService {
    private EffectiveClimbingRulesService() {
    }

    public static ClimbingRulesRuntimeView resolve(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            ClimbingRulesSavedData worldData = ClimbingRulesSavedData.get(serverPlayer.serverLevel().getServer());
            return PlayerRulesSessionStore.resolve(
                    serverPlayer,
                    worldData.runtimeView(),
                    worldData.policyRevision()
            );
        }
        if (player.level().isClientSide()) {
            return ClimbingRulesClientState.runtimeView();
        }
        return ClimbingRulesRuntimeView.defaults();
    }

    public static long policyRevision(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            ClimbingRulesSavedData worldData = ClimbingRulesSavedData.get(serverPlayer.serverLevel().getServer());
            return PlayerRulesSessionStore.policyRevision(serverPlayer, worldData.policyRevision());
        }
        return ClimbingRulesClientState.policyRevision();
    }
}
