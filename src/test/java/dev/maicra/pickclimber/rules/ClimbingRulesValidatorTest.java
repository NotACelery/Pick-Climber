package dev.maicra.pickclimber.rules;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClimbingRulesValidatorTest {
    private static final ResourceLocation STONE = ResourceLocation.fromNamespaceAndPath("minecraft", "stone");

    @Test
    void defaultProfileIsValidAndNameIsTrimmed() {
        ClimbingRulesProfile profile = ClimbingRulesProfile.defaults("  Route A  ");
        ClimbingRulesValidationResult result = ClimbingRulesValidator.validateAndNormalize(profile);

        assertTrue(result.valid());
        assertEquals("Route A", result.normalizedProfile().profileName());
    }

    @Test
    void duplicateClassificationIsRejected() {
        ClimbingRulesProfile profile = new ClimbingRulesProfile(
                1,
                "Conflict",
                Set.of(STONE),
                Set.of(STONE),
                Set.of(),
                UnlistedPolicy.USE_PICK_CLIMBER_DEFAULTS,
                100,
                true,
                false
        );

        ClimbingRulesValidationResult result = ClimbingRulesValidator.validateAndNormalize(profile);
        assertFalse(result.valid());
        assertTrue(result.issues().stream().anyMatch(issue -> issue.code().equals("duplicate_classification")));
    }

    @Test
    void profileOverBlockLimitIsRejected() {
        Set<ResourceLocation> blocks = new LinkedHashSet<>();
        for (int index = 0; index <= ClimbingRulesProfile.MAX_EXPLICIT_BLOCKS; index++) {
            blocks.add(ResourceLocation.fromNamespaceAndPath("test", "block_" + index));
        }
        ClimbingRulesProfile profile = new ClimbingRulesProfile(
                1,
                "Too Large",
                blocks,
                Set.of(),
                Set.of(),
                UnlistedPolicy.USE_PICK_CLIMBER_DEFAULTS,
                100,
                true,
                false
        );

        ClimbingRulesValidationResult result = ClimbingRulesValidator.validateAndNormalize(profile);
        assertFalse(result.valid());
        assertTrue(result.issues().stream().anyMatch(issue -> issue.code().equals("too_many_blocks")));
    }

    @Test
    void unsupportedFormatVersionIsRejected() {
        ClimbingRulesProfile profile = new ClimbingRulesProfile(
                99,
                "Future",
                Set.of(),
                Set.of(),
                Set.of(),
                UnlistedPolicy.USE_PICK_CLIMBER_DEFAULTS,
                100,
                true,
                false
        );

        ClimbingRulesValidationResult result = ClimbingRulesValidator.validateAndNormalize(profile);
        assertFalse(result.valid());
        assertTrue(result.issues().stream().anyMatch(issue -> issue.code().equals("unsupported_format_version")));
    }

    @Test
    void pickaxeWearOutsideSafeRangeIsRejected() {
        ClimbingRulesProfile profile = new ClimbingRulesProfile(
                1,
                "Too Expensive",
                Set.of(),
                Set.of(),
                Set.of(),
                UnlistedPolicy.USE_PICK_CLIMBER_DEFAULTS,
                101,
                true,
                false
        );

        ClimbingRulesValidationResult result = ClimbingRulesValidator.validateAndNormalize(profile);
        assertFalse(result.valid());
    }
    @Test
    void profileAtBlockLimitIsAccepted() {
        Set<ResourceLocation> blocks = new LinkedHashSet<>();
        for (int index = 0; index < ClimbingRulesProfile.MAX_EXPLICIT_BLOCKS; index++) {
            blocks.add(ResourceLocation.fromNamespaceAndPath("test", "block_" + index));
        }
        ClimbingRulesProfile profile = new ClimbingRulesProfile(
                1,
                "At Limit",
                blocks,
                Set.of(),
                Set.of(),
                UnlistedPolicy.USE_PICK_CLIMBER_DEFAULTS,
                100,
                true,
                false
        );

        assertTrue(ClimbingRulesValidator.validateAndNormalize(profile).valid());
    }

}
