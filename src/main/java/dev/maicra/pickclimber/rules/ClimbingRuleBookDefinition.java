package dev.maicra.pickclimber.rules;

import net.minecraft.world.item.DyeColor;

import java.util.Objects;

public record ClimbingRuleBookDefinition(
        int formatVersion,
        String bookName,
        DyeColor coverColor,
        ClimbingRulesProfile profile,
        RuleBookActivationMode activationMode,
        RuleBookScope scope,
        int durationSeconds
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
