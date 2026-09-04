package dev.maicra.pickclimber.climb;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ToolWearReasonTest {
    @Test
    void ruleWearOverridesStandardWallCostButNotCeilingCosts() {
        assertEquals(7, ToolWearReason.WALL_ATTACH.amount(1, 7));
        assertEquals(7, ToolWearReason.CLIMBING_BOOST.amount(1, 7));
        assertEquals(7, ToolWearReason.BRAKING_SUPPORT_ATTACH.amount(1, 7));
        assertEquals(20, ToolWearReason.CEILING_ATTACH.amount(1, 7));
        assertEquals(1, ToolWearReason.CEILING_SUSTAINED.amount(1, 7));
    }

    @Test
    void noRulesOverrideKeepsPickClimberBaseCosts() {
        assertEquals(15, ToolWearReason.WALL_ATTACH.amount(1, -1));
        assertEquals(20, ToolWearReason.CEILING_ATTACH.amount(1, -1));
        assertEquals(3, ToolWearReason.CEILING_SUSTAINED.amount(3, -1));
    }
}
