package dev.maicra.pickclimber.rules.network;

import dev.maicra.pickclimber.rules.ClimbingRulesClientState;
import dev.maicra.pickclimber.rules.TemporaryRuleBookClientState;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ClimbingRulesNetworking {
    private ClimbingRulesNetworking() {
    }

    public static void register(PayloadRegistrar registrar) {
        ClimbingRulesTableNetworking.register(registrar);
        ClimbingRuleDispenserNetworking.register(registrar);
        ClimbingRuleBookViewerNetworking.register(registrar);
        registrar.playToClient(
                WorldRulesStatePayload.TYPE,
                WorldRulesStatePayload.STREAM_CODEC,
                ClimbingRulesNetworking::handleWorldRulesState
        );
        registrar.playToClient(
                TemporaryRuleBookStatePayload.TYPE,
                TemporaryRuleBookStatePayload.STREAM_CODEC,
                (payload, context) -> TemporaryRuleBookClientState.apply(payload.expiresAtGameTime())
        );
        registrar.playToClient(
                PlayerRulesStatePayload.TYPE,
                PlayerRulesStatePayload.STREAM_CODEC,
                ClimbingRulesNetworking::handlePlayerRulesState
        );
    }

    private static void handleWorldRulesState(WorldRulesStatePayload payload, IPayloadContext context) {
        if (!payload.active()) {
            ClimbingRulesClientState.applyWorldDefaults(payload.policyRevision());
            return;
        }
        if (!RuleBookNetworkLimits.accepts(payload.definitionTag())) {
            ClimbingRulesClientState.applyWorldDefaults(payload.policyRevision());
            return;
        }
        ClimbingRulesClientState.applySerializedWorldDefinition(
                payload.definitionTag(),
                payload.temporary(),
                payload.expiresAtGameTime(),
                payload.policyRevision()
        );
    }

    private static void handlePlayerRulesState(PlayerRulesStatePayload payload, IPayloadContext context) {
        if (!payload.active()) {
            ClimbingRulesClientState.clearPlayerRules();
            return;
        }
        if (!RuleBookNetworkLimits.accepts(payload.definitionTag())) {
            ClimbingRulesClientState.clearPlayerRules();
            return;
        }
        ClimbingRulesClientState.applySerializedPlayerDefinition(
                payload.definitionTag(),
                payload.expiresAtGameTime(),
                payload.policyRevision()
        );
    }
}
