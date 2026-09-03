package dev.maicra.pickclimber.rules;

import java.util.List;

public record ClimbingRuleBookValidationResult(
        ClimbingRuleBookDefinition normalizedDefinition,
        List<ClimbingRulesValidationIssue> issues
) {
    public boolean valid() {
        return issues.isEmpty();
    }
}
