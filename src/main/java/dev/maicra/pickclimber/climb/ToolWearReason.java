package dev.maicra.pickclimber.climb;

public enum ToolWearReason {
    WALL_ATTACH(ClimbTuning.DURABILITY_COST, true),
    CEILING_ATTACH(ClimbTuning.CEILING_DURABILITY_COST, false),
    CLIMBING_BOOST(ClimbTuning.DURABILITY_COST, true),
    BRAKING_SUPPORT_ATTACH(ClimbTuning.DURABILITY_COST, true),
    BRAKING_BLOCK(ClimbTuning.BRAKING_DURABILITY_PER_BLOCK, false),
    CEILING_SUSTAINED(1, false);

    private final int baseAmount;
    private final boolean ruleConfigurable;

    ToolWearReason(int baseAmount, boolean ruleConfigurable) {
        this.baseAmount = baseAmount;
        this.ruleConfigurable = ruleConfigurable;
    }

    int amount(int units) {
        return baseAmount * Math.max(0, units);
    }

    int amount(int units, int configuredWear) {
        int safeUnits = Math.max(0, units);
        if (ruleConfigurable && configuredWear >= 0) {
            return Math.max(0, Math.min(100, configuredWear)) * safeUnits;
        }
        return amount(safeUnits);
    }
}
