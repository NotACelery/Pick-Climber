package dev.maicra.pickclimber.rules;

import java.util.Objects;

import net.minecraft.world.item.DyeColor;

public record ClimbingRuleBookDefinition(
        int formatVersion,
        String bookName,
        DyeColor coverColor,
        ClimbingRulesProfile profile,
        RuleBookActivationMode activationMode,
        RuleBookScope scope,
        int durationSeconds,
        String authorUuid,
        String authorName
) {
    public static final int CURRENT_FORMAT_VERSION = 2;
    public static final int MAX_BOOK_NAME_LENGTH = 64;
    public static final int MAX_SERIALIZED_BYTES = 512 * 1024;

    public ClimbingRuleBookDefinition {
        bookName = Objects.requireNonNull(bookName, "bookName");
        coverColor = Objects.requireNonNull(coverColor, "coverColor");
        profile = Objects.requireNonNull(profile, "profile");
        activationMode = Objects.requireNonNull(activationMode, "activationMode");
        scope = Objects.requireNonNull(scope, "scope");
        authorUuid = authorUuid == null ? "" : authorUuid;
        authorName = authorName == null ? "" : authorName;
    }

    public ClimbingRuleBookDefinition(
            int formatVersion,
            String bookName,
            DyeColor coverColor,
            ClimbingRulesProfile profile,
            RuleBookActivationMode activationMode,
            RuleBookScope scope,
            int durationSeconds
    ) {
        this(formatVersion, bookName, coverColor, profile, activationMode, scope, durationSeconds, "", "");
    }

    public ClimbingRuleBookDefinition withAuthor(String uuid, String name) {
        return new ClimbingRuleBookDefinition(
                formatVersion, bookName, coverColor, profile, activationMode, scope, durationSeconds, uuid, name
        );
    }

    public static ClimbingRuleBookDefinition permanentWorld(String bookName, ClimbingRulesProfile profile) {
        return new ClimbingRuleBookDefinition(
                CURRENT_FORMAT_VERSION,
                bookName,
                DyeColor.WHITE,
                profile,
                RuleBookActivationMode.PERMANENT,
                RuleBookScope.WORLD,
                0
        );
    }
}
