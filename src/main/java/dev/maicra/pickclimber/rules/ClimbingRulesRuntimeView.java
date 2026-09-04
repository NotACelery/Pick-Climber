package dev.maicra.pickclimber.rules;

import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class ClimbingRulesRuntimeView {
    private static final ClimbingRulesRuntimeView DEFAULTS = new ClimbingRulesRuntimeView(
            false,
            "",
            Map.of(),
            UnlistedPolicy.USE_PICK_CLIMBER_DEFAULTS,
            ClimbingRulesProfile.DEFAULT_PICKAXE_WEAR,
            true,
            false
    );

    private final boolean active;
    private final String profileName;
    private final Map<ResourceLocation, SurfaceClassification> overrides;
    private final UnlistedPolicy unlistedPolicy;
    private final int pickaxeWear;
    private final boolean playerMiningEnabled;
    private final boolean unmineableTerminals;

    private ClimbingRulesRuntimeView(
            boolean active,
            String profileName,
            Map<ResourceLocation, SurfaceClassification> overrides,
            UnlistedPolicy unlistedPolicy,
            int pickaxeWear,
            boolean playerMiningEnabled,
            boolean unmineableTerminals
    ) {
        this.active = active;
        this.profileName = profileName;
        this.overrides = Map.copyOf(overrides);
        this.unlistedPolicy = unlistedPolicy;
        this.pickaxeWear = pickaxeWear;
        this.playerMiningEnabled = playerMiningEnabled;
        this.unmineableTerminals = unmineableTerminals;
    }

    public static ClimbingRulesRuntimeView defaults() {
        return DEFAULTS;
    }

    public static ClimbingRulesRuntimeView fromProfile(ClimbingRulesProfile profile) {
        Map<ResourceLocation, SurfaceClassification> overrides = new HashMap<>();
        profile.stableBlocks().forEach(id -> overrides.put(id, SurfaceClassification.STABLE));
        profile.unstableBlocks().forEach(id -> overrides.put(id, SurfaceClassification.UNSTABLE));
        profile.unclimbableBlocks().forEach(id -> overrides.put(id, SurfaceClassification.UNCLIMBABLE));
        return new ClimbingRulesRuntimeView(
                true,
                profile.profileName(),
                overrides,
                profile.unlistedPolicy(),
                profile.pickaxeWear(),
                profile.playerMiningEnabled(),
                profile.unmineableTerminals()
        );
    }

    public boolean active() {
        return active;
    }

    public String profileName() {
        return profileName;
    }

    public Optional<SurfaceClassification> classificationOverride(ResourceLocation blockId) {
        return Optional.ofNullable(overrides.get(blockId));
    }

    public UnlistedPolicy unlistedPolicy() {
        return unlistedPolicy;
    }

    public int pickaxeWear() {
        return pickaxeWear;
    }

    public boolean playerMiningEnabled() {
        return playerMiningEnabled;
    }

    public boolean unmineableTerminals() {
        return unmineableTerminals;
    }
}
