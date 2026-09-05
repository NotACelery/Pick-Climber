package dev.maicra.pickclimber.rules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.resources.ResourceLocation;

public final class ClimbingRulesValidator {
    private ClimbingRulesValidator() {
    }

    public static ClimbingRulesValidationResult validateAndNormalize(ClimbingRulesProfile profile) {
        String normalizedName = profile.profileName().trim();
        ClimbingRulesProfile normalized = new ClimbingRulesProfile(
                profile.formatVersion(),
                normalizedName,
                profile.stableBlocks(),
                profile.unstableBlocks(),
                profile.unclimbableBlocks(),
                profile.unlistedPolicy(),
                profile.pickaxeWear(),
                profile.playerMiningEnabled(),
                profile.unmineableTerminals()
        );

        List<ClimbingRulesValidationIssue> issues = new ArrayList<>();
        validateFormat(normalized, issues);
        validateName(normalized, issues);
        validatePickaxeWear(normalized, issues);
        validateBlockCount(normalized, issues);
        validateClassificationConflicts(normalized, issues);
        return new ClimbingRulesValidationResult(normalized, issues);
    }

    private static void validateFormat(
            ClimbingRulesProfile profile,
            List<ClimbingRulesValidationIssue> issues
    ) {
        if (profile.formatVersion() != ClimbingRulesProfile.CURRENT_FORMAT_VERSION) {
            issues.add(new ClimbingRulesValidationIssue(
                    "unsupported_format_version",
                    "Unsupported climbing rules format version: " + profile.formatVersion()
            ));
        }
    }

    private static void validateName(
            ClimbingRulesProfile profile,
            List<ClimbingRulesValidationIssue> issues
    ) {
        if (profile.profileName().isEmpty()) {
            issues.add(new ClimbingRulesValidationIssue("empty_name", "Profile name cannot be empty"));
        } else if (profile.profileName().length() > ClimbingRulesProfile.MAX_PROFILE_NAME_LENGTH) {
            issues.add(new ClimbingRulesValidationIssue(
                    "name_too_long",
                    "Profile name exceeds " + ClimbingRulesProfile.MAX_PROFILE_NAME_LENGTH + " characters"
            ));
        }
    }

    private static void validatePickaxeWear(
            ClimbingRulesProfile profile,
            List<ClimbingRulesValidationIssue> issues
    ) {
        int wear = profile.pickaxeWear();
        if (wear < 0 || wear > ClimbingRulesProfile.MAX_PICKAXE_WEAR) {
            issues.add(new ClimbingRulesValidationIssue(
                    "invalid_pickaxe_wear",
                    "Pickaxe wear must be between 0 and " + ClimbingRulesProfile.MAX_PICKAXE_WEAR
            ));
        }
    }

    private static void validateBlockCount(
            ClimbingRulesProfile profile,
            List<ClimbingRulesValidationIssue> issues
    ) {
        int total = profile.stableBlocks().size()
                + profile.unstableBlocks().size()
                + profile.unclimbableBlocks().size();
        if (total > ClimbingRulesProfile.MAX_EXPLICIT_BLOCKS) {
            issues.add(new ClimbingRulesValidationIssue(
                    "too_many_blocks",
                    "Profile exceeds the maximum of " + ClimbingRulesProfile.MAX_EXPLICIT_BLOCKS + " block entries"
            ));
        }
    }

    private static void validateClassificationConflicts(
            ClimbingRulesProfile profile,
            List<ClimbingRulesValidationIssue> issues
    ) {
        Set<ResourceLocation> seen = new HashSet<>();
        addClassification(profile.stableBlocks(), "stable", seen, issues);
        addClassification(profile.unstableBlocks(), "unstable", seen, issues);
        addClassification(profile.unclimbableBlocks(), "unclimbable", seen, issues);
    }

    private static void addClassification(
            Set<ResourceLocation> blocks,
            String classification,
            Set<ResourceLocation> seen,
            List<ClimbingRulesValidationIssue> issues
    ) {
        for (ResourceLocation block : blocks) {
            if (!seen.add(block)) {
                issues.add(new ClimbingRulesValidationIssue(
                        "duplicate_classification",
                        block + " appears in more than one surface classification, including " + classification
                ));
            }
        }
    }
}
