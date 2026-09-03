package dev.maicra.pickclimber.rules;

import java.util.Optional;

public record PlayerRulesSnapshot(
        Optional<ClimbingRuleBookDefinition> definition,
        long expiresAtGameTime,
        long policyRevision
) {
    public PlayerRulesSnapshot {
        definition = definition == null ? Optional.empty() : definition;
        expiresAtGameTime = Math.max(0L, expiresAtGameTime);
    }

    public static PlayerRulesSnapshot inactive() {
        return new PlayerRulesSnapshot(Optional.empty(), 0L, 0L);
    }

    public boolean active() {
        return definition.isPresent();
    }
}
