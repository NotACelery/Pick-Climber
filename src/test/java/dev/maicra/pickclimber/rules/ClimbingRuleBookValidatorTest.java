package dev.maicra.pickclimber.rules;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClimbingRuleBookValidatorTest {
    @Test
    void permanentRuleBookIsNormalizedToWorldWithoutDuration() {
        ClimbingRuleBookDefinition source = new ClimbingRuleBookDefinition(
                ClimbingRuleBookDefinition.CURRENT_FORMAT_VERSION,
                "Permanent",
                DyeColor.WHITE,
                ClimbingRulesProfile.defaults("Permanent"),
                RuleBookActivationMode.PERMANENT,
                RuleBookScope.PLAYER,
                30
        );

        ClimbingRuleBookValidationResult result = ClimbingRuleBookValidator.validateAndNormalize(source);

        assertTrue(result.valid());
        assertEquals(RuleBookScope.WORLD, result.normalizedDefinition().scope());
        assertEquals(0, result.normalizedDefinition().durationSeconds());
    }

    @Test
    void temporaryRuleBookRequiresPositiveDuration() {
        ClimbingRuleBookDefinition source = new ClimbingRuleBookDefinition(
                ClimbingRuleBookDefinition.CURRENT_FORMAT_VERSION,
                "Temporary",
                DyeColor.RED,
                ClimbingRulesProfile.defaults("Temporary"),
                RuleBookActivationMode.TEMPORARY,
                RuleBookScope.PLAYER,
                0
        );

        ClimbingRuleBookValidationResult result = ClimbingRuleBookValidator.validateAndNormalize(source);

        assertFalse(result.valid());
    }
    @Test
    void ruleBookNameMustBePortableAcrossFilesystems() {
        ClimbingRuleBookDefinition source = new ClimbingRuleBookDefinition(
                ClimbingRuleBookDefinition.CURRENT_FORMAT_VERSION,
                "Route: Forbidden",
                DyeColor.WHITE,
                ClimbingRulesProfile.defaults("Route: Forbidden"),
                RuleBookActivationMode.PERMANENT,
                RuleBookScope.WORLD,
                0
        );

        ClimbingRuleBookValidationResult result = ClimbingRuleBookValidator.validateAndNormalize(source);

        assertFalse(result.valid());
        assertTrue(result.issues().stream().anyMatch(issue -> issue.code().equals("book_name_not_portable")));
    }

    @Test
    void profileNameIsCanonicalizedToBookName() {
        ClimbingRuleBookDefinition source = new ClimbingRuleBookDefinition(
                ClimbingRuleBookDefinition.CURRENT_FORMAT_VERSION,
                "Visible Name",
                DyeColor.WHITE,
                ClimbingRulesProfile.defaults("Hidden Name"),
                RuleBookActivationMode.PERMANENT,
                RuleBookScope.WORLD,
                0
        );

        ClimbingRuleBookValidationResult result = ClimbingRuleBookValidator.validateAndNormalize(source);

        assertTrue(result.valid());
        assertEquals("Visible Name", result.normalizedDefinition().profile().profileName());
    }

    @Test
    void oversizedSerializedRuleBookIsRejected() {
        Set<ResourceLocation> blocks = new LinkedHashSet<>();
        String suffix = "x".repeat(90);
        for (int index = 0; index < 6000; index++) {
            blocks.add(ResourceLocation.fromNamespaceAndPath("test", "block_" + index + "_" + suffix));
        }
        ClimbingRulesProfile profile = new ClimbingRulesProfile(
                ClimbingRulesProfile.CURRENT_FORMAT_VERSION,
                "Oversized",
                blocks,
                Set.of(),
                Set.of(),
                UnlistedPolicy.USE_PICK_CLIMBER_DEFAULTS,
                100,
                true,
                false
        );
        ClimbingRuleBookDefinition definition = new ClimbingRuleBookDefinition(
                ClimbingRuleBookDefinition.CURRENT_FORMAT_VERSION,
                "Oversized",
                DyeColor.WHITE,
                profile,
                RuleBookActivationMode.PERMANENT,
                RuleBookScope.WORLD,
                0
        );

        ClimbingRuleBookValidationResult result = ClimbingRuleBookValidator.validateAndNormalize(definition);
        assertFalse(result.valid());
        assertTrue(result.issues().stream().anyMatch(issue -> issue.code().equals("rule_book_too_large")));
    }

}
