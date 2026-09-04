package dev.maicra.pickclimber.rules;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClimbingRulesProfileBehaviorTest {
    @Test
    void mechanicalEqualityIgnoresDisplayNameButIncludesTerminalPolicy() {
        ClimbingRulesProfile first = new ClimbingRulesProfile(
                ClimbingRulesProfile.CURRENT_FORMAT_VERSION,
                "Route A",
                Set.of(ResourceLocation.fromNamespaceAndPath("minecraft", "stone")),
                Set.of(),
                Set.of(),
                UnlistedPolicy.USE_PICK_CLIMBER_DEFAULTS,
                100,
                true,
                false
        );
        ClimbingRulesProfile renamed = new ClimbingRulesProfile(
                ClimbingRulesProfile.CURRENT_FORMAT_VERSION,
                "Route B",
                first.stableBlocks(),
                first.unstableBlocks(),
                first.unclimbableBlocks(),
                first.unlistedPolicy(),
                first.pickaxeWear(),
                first.playerMiningEnabled(),
                false
        );
        ClimbingRulesProfile lockedTerminals = new ClimbingRulesProfile(
                ClimbingRulesProfile.CURRENT_FORMAT_VERSION,
                "Route B",
                first.stableBlocks(),
                first.unstableBlocks(),
                first.unclimbableBlocks(),
                first.unlistedPolicy(),
                first.pickaxeWear(),
                first.playerMiningEnabled(),
                true
        );

        assertTrue(first.mechanicallyEquals(renamed));
        assertFalse(first.mechanicallyEquals(lockedTerminals));
    }
}
