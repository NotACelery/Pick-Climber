package dev.maicra.pickclimber.rules;

public record RuleBookApplicationResult(
        boolean success,
        boolean refreshed,
        String messageKey
) {
    public static RuleBookApplicationResult applied(String messageKey) {
        return new RuleBookApplicationResult(true, false, messageKey);
    }

    public static RuleBookApplicationResult refreshed(String messageKey) {
        return new RuleBookApplicationResult(true, true, messageKey);
    }

    public static RuleBookApplicationResult rejected(String messageKey) {
        return new RuleBookApplicationResult(false, false, messageKey);
    }
}
