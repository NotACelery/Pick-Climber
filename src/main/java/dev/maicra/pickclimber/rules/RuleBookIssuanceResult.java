package dev.maicra.pickclimber.rules;

public record RuleBookIssuanceResult(boolean success, String messageKey) {
    public static RuleBookIssuanceResult issued(String messageKey) {
        return new RuleBookIssuanceResult(true, messageKey);
    }

    public static RuleBookIssuanceResult rejected(String messageKey) {
        return new RuleBookIssuanceResult(false, messageKey);
    }
}
