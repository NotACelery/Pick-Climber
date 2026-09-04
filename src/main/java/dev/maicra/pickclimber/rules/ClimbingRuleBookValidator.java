package dev.maicra.pickclimber.rules;

import java.util.ArrayList;
import java.util.List;

public final class ClimbingRuleBookValidator {
    private ClimbingRuleBookValidator() {
    }

    public static ClimbingRuleBookValidationResult validateAndNormalize(ClimbingRuleBookDefinition definition) {
        List<ClimbingRulesValidationIssue> issues = new ArrayList<>();
        String name = definition.bookName().trim();
        ClimbingRulesValidationResult profileValidation = ClimbingRulesValidator.validateAndNormalize(
                definition.profile()
        );
        issues.addAll(profileValidation.issues());

        if (definition.formatVersion() != ClimbingRuleBookDefinition.CURRENT_FORMAT_VERSION) {
            issues.add(new ClimbingRulesValidationIssue(
                    "unsupported_rule_book_format_version",
                    "Unsupported Rule Book format version: " + definition.formatVersion()
            ));
        }
        if (name.isEmpty()) {
            issues.add(new ClimbingRulesValidationIssue("empty_book_name", "Rule Book name cannot be empty"));
        } else if (name.length() > ClimbingRuleBookDefinition.MAX_BOOK_NAME_LENGTH) {
            issues.add(new ClimbingRulesValidationIssue(
                    "book_name_too_long",
                    "Rule Book name exceeds " + ClimbingRuleBookDefinition.MAX_BOOK_NAME_LENGTH + " characters"
            ));
        } else if (RuleBookNamePolicy.portableFileStem(name).isEmpty()) {
            issues.add(new ClimbingRulesValidationIssue(
                    "book_name_not_portable",
                    "Rule Book name is not portable across supported filesystems"
            ));
        }

        RuleBookScope normalizedScope = definition.scope();
        int normalizedDuration = definition.durationSeconds();
        if (definition.activationMode() == RuleBookActivationMode.PERMANENT) {
            normalizedScope = RuleBookScope.WORLD;
            normalizedDuration = 0;
        } else if (normalizedDuration <= 0) {
            issues.add(new ClimbingRulesValidationIssue(
                    "temporary_duration_required",
                    "Temporary Rule Books require a duration greater than zero"
            ));
        }

        ClimbingRulesProfile normalizedProfile = withProfileName(
                profileValidation.normalizedProfile(),
                name
        );
        ClimbingRuleBookDefinition normalized = new ClimbingRuleBookDefinition(
                definition.formatVersion(),
                name,
                definition.coverColor(),
                normalizedProfile,
                definition.activationMode(),
                normalizedScope,
                normalizedDuration,
                definition.authorUuid(),
                definition.authorName()
        );
        ClimbingRuleBookCodec.encodeToNbt(normalized).result().ifPresentOrElse(tag -> {
            if (tag.sizeInBytes() > ClimbingRuleBookDefinition.MAX_SERIALIZED_BYTES) {
                issues.add(new ClimbingRulesValidationIssue(
                        "rule_book_too_large",
                        "Rule Book exceeds the maximum serialized size of "
                                + ClimbingRuleBookDefinition.MAX_SERIALIZED_BYTES
                                + " bytes"
                ));
            }
        }, () -> issues.add(new ClimbingRulesValidationIssue(
                "rule_book_encode_failed",
                "Rule Book could not be serialized"
        )));
        return new ClimbingRuleBookValidationResult(normalized, List.copyOf(issues));
    }

    private static ClimbingRulesProfile withProfileName(
            ClimbingRulesProfile profile,
            String profileName
    ) {
        return new ClimbingRulesProfile(
                profile.formatVersion(),
                profileName,
                profile.stableBlocks(),
                profile.unstableBlocks(),
                profile.unclimbableBlocks(),
                profile.unlistedPolicy(),
                profile.pickaxeWear(),
                profile.playerMiningEnabled(),
                profile.unmineableTerminals()
        );
    }
}
