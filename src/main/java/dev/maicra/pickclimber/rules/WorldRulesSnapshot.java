package dev.maicra.pickclimber.rules;

import java.util.Optional;

public record WorldRulesSnapshot(
        Optional<ClimbingRuleBookDefinition> permanentDefinition,
        Optional<ClimbingRuleBookDefinition> temporaryDefinition,
        long temporaryExpiresAtGameTime,
        long policyRevision
) {
    public WorldRulesSnapshot {
        permanentDefinition = permanentDefinition == null ? Optional.empty() : permanentDefinition;
        temporaryDefinition = temporaryDefinition == null ? Optional.empty() : temporaryDefinition;
        temporaryExpiresAtGameTime = Math.max(0L, temporaryExpiresAtGameTime);
        policyRevision = Math.max(0L, policyRevision);
    }

    public Optional<ClimbingRuleBookDefinition> effectiveDefinition() {
        return temporaryDefinition.isPresent() ? temporaryDefinition : permanentDefinition;
    }

    public boolean active() {
        return effectiveDefinition().isPresent();
    }

    public boolean temporaryActive() {
        return temporaryDefinition.isPresent();
    }

    public int configuredDurationSeconds() {
        return temporaryDefinition.map(ClimbingRuleBookDefinition::durationSeconds).orElse(0);
    }
}
