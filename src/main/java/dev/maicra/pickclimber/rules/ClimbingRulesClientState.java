package dev.maicra.pickclimber.rules;

import java.util.Optional;

import com.mojang.logging.LogUtils;

import net.minecraft.nbt.CompoundTag;

import org.slf4j.Logger;

public final class ClimbingRulesClientState {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static volatile ClimbingRulesRuntimeView worldRuntimeView = ClimbingRulesRuntimeView.defaults();
    private static volatile Optional<ClimbingRuleBookDefinition> worldDefinition = Optional.empty();
    private static volatile boolean worldTemporary;
    private static volatile long worldExpiresAtGameTime;
    private static volatile long worldPolicyRevision;

    private static volatile ClimbingRulesRuntimeView playerRuntimeView = ClimbingRulesRuntimeView.defaults();
    private static volatile Optional<ClimbingRuleBookDefinition> playerDefinition = Optional.empty();
    private static volatile long playerExpiresAtGameTime;
    private static volatile long playerPolicyRevision;

    private ClimbingRulesClientState() {
    }

    public static ClimbingRulesRuntimeView runtimeView() {
        return playerDefinition.isPresent() ? playerRuntimeView : worldRuntimeView;
    }

    public static ClimbingRulesRuntimeView worldRuntimeView() {
        return worldRuntimeView;
    }

    public static Optional<ClimbingRulesProfile> activeProfile() {
        return activeDefinition().map(ClimbingRuleBookDefinition::profile);
    }

    public static Optional<ClimbingRuleBookDefinition> activeDefinition() {
        return playerDefinition.isPresent() ? playerDefinition : worldDefinition;
    }

    public static Optional<ClimbingRuleBookDefinition> worldActiveDefinition() {
        return worldDefinition;
    }

    public static Optional<ClimbingRuleBookDefinition> playerActiveDefinition() {
        return playerDefinition;
    }

    public static boolean temporary() {
        return playerDefinition.isPresent() || worldTemporary;
    }

    public static boolean playerTemporary() {
        return playerDefinition.isPresent();
    }

    public static boolean worldTemporary() {
        return worldTemporary;
    }

    public static long expiresAtGameTime() {
        if (playerDefinition.isPresent()) {
            return playerExpiresAtGameTime;
        }
        return worldTemporary ? worldExpiresAtGameTime : 0L;
    }

    public static long worldExpiresAtGameTime() {
        return worldTemporary ? worldExpiresAtGameTime : 0L;
    }

    public static long playerExpiresAtGameTime() {
        return playerDefinition.isPresent() ? playerExpiresAtGameTime : 0L;
    }

    public static long policyRevision() {
        return playerDefinition.isPresent() ? playerPolicyRevision : worldPolicyRevision;
    }

    public static long worldPolicyRevision() {
        return worldPolicyRevision;
    }

    public static void applySerializedWorldDefinition(
            CompoundTag definitionTag,
            boolean temporaryState,
            long expiresAt,
            long revision
    ) {
        clearWorldRuntimeOnly();
        worldPolicyRevision = Math.max(0L, revision);
        ClimbingRuleBookCodec.decodeFromNbt(definitionTag)
                .resultOrPartial(message -> LOGGER.warn("Ignoring invalid world rules sync: {}", message))
                .ifPresent(definition -> applyWorldDefinition(definition, temporaryState, expiresAt));
    }

    public static void applyWorldDefinition(
            ClimbingRuleBookDefinition definition,
            boolean temporaryState,
            long expiresAt
    ) {
        ClimbingRuleBookValidationResult validation = ClimbingRuleBookValidator.validateAndNormalize(definition);
        if (!validation.valid()) {
            clearWorldRuntimeOnly();
            LOGGER.warn("Ignoring invalid world rules sync with {} validation issue(s)", validation.issues().size());
            return;
        }
        ClimbingRuleBookDefinition normalized = validation.normalizedDefinition();
        worldDefinition = Optional.of(normalized);
        worldRuntimeView = ClimbingRulesRuntimeView.fromProfile(normalized.profile());
        worldTemporary = temporaryState;
        worldExpiresAtGameTime = temporaryState ? Math.max(0L, expiresAt) : 0L;
    }

    public static void applyWorldDefaults(long revision) {
        clearWorldRuntimeOnly();
        worldPolicyRevision = Math.max(0L, revision);
    }

    public static void applySerializedPlayerDefinition(
            CompoundTag definitionTag,
            long expiresAt,
            long revision
    ) {
        clearPlayerRuntimeOnly();
        ClimbingRuleBookCodec.decodeFromNbt(definitionTag)
                .resultOrPartial(message -> LOGGER.warn("Ignoring invalid PLAYER rules sync: {}", message))
                .ifPresent(definition -> applyPlayerDefinition(definition, expiresAt, revision));
    }

    public static void applyPlayerDefinition(
            ClimbingRuleBookDefinition definition,
            long expiresAt,
            long revision
    ) {
        ClimbingRuleBookValidationResult validation = ClimbingRuleBookValidator.validateAndNormalize(definition);
        if (!validation.valid()) {
            clearPlayerRuntimeOnly();
            LOGGER.warn("Ignoring invalid PLAYER rules sync with {} validation issue(s)", validation.issues().size());
            return;
        }
        ClimbingRuleBookDefinition normalized = validation.normalizedDefinition();
        if (normalized.activationMode() != RuleBookActivationMode.TEMPORARY
                || normalized.scope() != RuleBookScope.PLAYER) {
            clearPlayerRuntimeOnly();
            LOGGER.warn("Ignoring PLAYER rules sync with incompatible activation/scope metadata");
            return;
        }
        playerDefinition = Optional.of(normalized);
        playerRuntimeView = ClimbingRulesRuntimeView.fromProfile(normalized.profile());
        playerExpiresAtGameTime = Math.max(0L, expiresAt);
        playerPolicyRevision = Math.max(0L, revision);
    }

    public static void clearPlayerRules() {
        clearPlayerRuntimeOnly();
    }

    public static void clear() {
        clearWorldRuntimeOnly();
        clearPlayerRuntimeOnly();
        worldPolicyRevision = 0L;
    }

    private static void clearWorldRuntimeOnly() {
        worldDefinition = Optional.empty();
        worldRuntimeView = ClimbingRulesRuntimeView.defaults();
        worldTemporary = false;
        worldExpiresAtGameTime = 0L;
    }

    private static void clearPlayerRuntimeOnly() {
        playerDefinition = Optional.empty();
        playerRuntimeView = ClimbingRulesRuntimeView.defaults();
        playerExpiresAtGameTime = 0L;
        playerPolicyRevision = 0L;
    }
}
