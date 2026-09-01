package dev.maicra.pickclimber.climb;

public enum ToolWearReason {
    WALL_ATTACH(ClimbTuning.DURABILITY_COST),
    CEILING_ATTACH(ClimbTuning.CEILING_DURABILITY_COST),
    CLIMBING_BOOST(ClimbTuning.DURABILITY_COST),
    BRAKING_SUPPORT_ATTACH(ClimbTuning.DURABILITY_COST),
    BRAKING_BLOCK(ClimbTuning.BRAKING_DURABILITY_PER_BLOCK),
    CEILING_SUSTAINED(1);

    private final int baseAmount;

    ToolWearReason(int baseAmount) {
        this.baseAmount = baseAmount;
    }

    int amount(int units) {
        return baseAmount * Math.max(0, units);
    }
}
