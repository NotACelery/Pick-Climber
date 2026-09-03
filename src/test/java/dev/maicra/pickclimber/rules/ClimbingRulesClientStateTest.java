package dev.maicra.pickclimber.rules;

import net.minecraft.world.item.DyeColor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClimbingRulesClientStateTest {
    @AfterEach
    void clearClientState() {
        ClimbingRulesClientState.clear();
    }

    @Test
    void playerTemporaryOverridesWorldAndFallsBackWhenCleared() {
        ClimbingRuleBookDefinition world = ClimbingRuleBookDefinition.permanentWorld(
                "World",
                profile("World", 80)
        );
        ClimbingRuleBookDefinition player = new ClimbingRuleBookDefinition(
                ClimbingRuleBookDefinition.CURRENT_FORMAT_VERSION,
                "Player",
                DyeColor.RED,
                profile("Player", 40),
                RuleBookActivationMode.TEMPORARY,
                RuleBookScope.PLAYER,
                30
        );

        ClimbingRulesClientState.applyWorldDefinition(world, false, 0L);
        ClimbingRulesClientState.applyPlayerDefinition(player, 600L, 999L);

        assertEquals("Player", ClimbingRulesClientState.runtimeView().profileName());
        assertEquals(40, ClimbingRulesClientState.runtimeView().durabilityMultiplierPercent());
        assertEquals(999L, ClimbingRulesClientState.policyRevision());

        ClimbingRulesClientState.clearPlayerRules();

        assertEquals("World", ClimbingRulesClientState.runtimeView().profileName());
        assertEquals(80, ClimbingRulesClientState.runtimeView().durabilityMultiplierPercent());
    }

    private static ClimbingRulesProfile profile(String name, int durabilityPercent) {
        return new ClimbingRulesProfile(
                ClimbingRulesProfile.CURRENT_FORMAT_VERSION,
                name,
                java.util.Set.of(),
                java.util.Set.of(),
                java.util.Set.of(),
                UnlistedPolicy.USE_PICK_CLIMBER_DEFAULTS,
                durabilityPercent,
                true,
                false
        );
    }
}
