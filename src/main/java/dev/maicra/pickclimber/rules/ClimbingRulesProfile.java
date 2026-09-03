package dev.maicra.pickclimber.rules;

import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.Set;

public record ClimbingRulesProfile(
        int formatVersion,
        String profileName,
        Set<ResourceLocation> stableBlocks,
        Set<ResourceLocation> unstableBlocks,
        Set<ResourceLocation> unclimbableBlocks,
        UnlistedPolicy unlistedPolicy,
        int durabilityMultiplierPercent,
        boolean playerMiningEnabled,
        boolean unmineableTerminals
) {
    public static final int CURRENT_FORMAT_VERSION = 1;
    public static final int DEFAULT_DURABILITY_MULTIPLIER_PERCENT = 100;
    public static final int MAX_DURABILITY_MULTIPLIER_PERCENT = 500;
    public static final int MAX_PROFILE_NAME_LENGTH = 64;
    public static final int MAX_EXPLICIT_BLOCKS = 8192;

    public ClimbingRulesProfile {
        profileName = Objects.requireNonNull(profileName, "profileName");
        stableBlocks = Set.copyOf(Objects.requireNonNull(stableBlocks, "stableBlocks"));
        unstableBlocks = Set.copyOf(Objects.requireNonNull(unstableBlocks, "unstableBlocks"));
        unclimbableBlocks = Set.copyOf(Objects.requireNonNull(unclimbableBlocks, "unclimbableBlocks"));
        unlistedPolicy = Objects.requireNonNull(unlistedPolicy, "unlistedPolicy");
    }

    public boolean mechanicallyEquals(ClimbingRulesProfile other) {
        return other != null
                && stableBlocks.equals(other.stableBlocks)
                && unstableBlocks.equals(other.unstableBlocks)
                && unclimbableBlocks.equals(other.unclimbableBlocks)
                && unlistedPolicy == other.unlistedPolicy
                && durabilityMultiplierPercent == other.durabilityMultiplierPercent
                && playerMiningEnabled == other.playerMiningEnabled
                && unmineableTerminals == other.unmineableTerminals;
    }

    public static ClimbingRulesProfile defaults(String profileName) {
        return new ClimbingRulesProfile(
                CURRENT_FORMAT_VERSION,
                profileName,
                Set.of(),
                Set.of(),
                Set.of(),
                UnlistedPolicy.USE_PICK_CLIMBER_DEFAULTS,
                DEFAULT_DURABILITY_MULTIPLIER_PERCENT,
                true,
                false
        );
    }
}
