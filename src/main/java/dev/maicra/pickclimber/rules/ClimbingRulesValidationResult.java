package dev.maicra.pickclimber.rules;

import java.util.List;

public record ClimbingRulesValidationResult(
        ClimbingRulesProfile normalizedProfile,
        List<ClimbingRulesValidationIssue> issues
) {
    public ClimbingRulesValidationResult {
        issues = List.copyOf(issues);
    }

    public boolean valid() {
        return issues.isEmpty();
    }
}
