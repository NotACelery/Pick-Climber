package dev.maicra.pickclimber.rules.persistence;

import com.mojang.logging.LogUtils;
import dev.maicra.pickclimber.rules.ClimbingRuleBookCodec;
import dev.maicra.pickclimber.rules.ClimbingRuleBookDefinition;
import dev.maicra.pickclimber.rules.ClimbingRuleBookValidationResult;
import dev.maicra.pickclimber.rules.ClimbingRuleBookValidator;
import dev.maicra.pickclimber.rules.ClimbingRulesProfile;
import dev.maicra.pickclimber.rules.ClimbingRulesProfileCodec;
import dev.maicra.pickclimber.rules.ClimbingRulesRuntimeView;
import dev.maicra.pickclimber.rules.ClimbingRulesValidationResult;
import dev.maicra.pickclimber.rules.ClimbingRulesValidator;
import dev.maicra.pickclimber.rules.RuleBookActivationMode;
import dev.maicra.pickclimber.rules.RuleBookScope;
import dev.maicra.pickclimber.rules.WorldRulesSnapshot;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import org.slf4j.Logger;

import java.util.Optional;

public final class ClimbingRulesSavedData extends SavedData {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String DATA_NAME = "pickclimber_climbing_rules";
    private static final String PERMANENT_DEFINITION_KEY = "permanent_world_definition";
    private static final String TEMPORARY_DEFINITION_KEY = "temporary_world_definition";
    private static final String TEMPORARY_EXPIRES_AT_KEY = "temporary_expires_at_game_time";
    private static final String LEGACY_ACTIVE_PROFILE_KEY = "active_profile";
    private static final String POLICY_REVISION_KEY = "policy_revision";
    private static final Factory<ClimbingRulesSavedData> FACTORY = new Factory<>(
            ClimbingRulesSavedData::new,
            ClimbingRulesSavedData::load
    );

    private Optional<ClimbingRuleBookDefinition> permanentDefinition;
    private Optional<ClimbingRuleBookDefinition> temporaryDefinition;
    private long temporaryExpiresAtGameTime;
    private ClimbingRulesRuntimeView runtimeView;
    private long policyRevision;

    public ClimbingRulesSavedData() {
        this(Optional.empty(), Optional.empty(), 0L, 0L);
    }

    private ClimbingRulesSavedData(
            Optional<ClimbingRuleBookDefinition> permanentDefinition,
            Optional<ClimbingRuleBookDefinition> temporaryDefinition,
            long temporaryExpiresAtGameTime,
            long policyRevision
    ) {
        this.permanentDefinition = permanentDefinition;
        this.temporaryDefinition = temporaryDefinition;
        this.temporaryExpiresAtGameTime = Math.max(0L, temporaryExpiresAtGameTime);
        this.policyRevision = Math.max(0L, policyRevision);
        rebuildRuntimeView();
    }

    public static ClimbingRulesSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    public Optional<ClimbingRuleBookDefinition> permanentDefinition() {
        return permanentDefinition;
    }

    public Optional<ClimbingRuleBookDefinition> temporaryDefinition() {
        return temporaryDefinition;
    }

    public Optional<ClimbingRuleBookDefinition> effectiveDefinition() {
        return temporaryDefinition.isPresent() ? temporaryDefinition : permanentDefinition;
    }

    public Optional<ClimbingRulesProfile> activeProfile() {
        return effectiveDefinition().map(ClimbingRuleBookDefinition::profile);
    }

    public ClimbingRulesRuntimeView runtimeView() {
        return runtimeView;
    }

    public long temporaryExpiresAtGameTime() {
        return temporaryExpiresAtGameTime;
    }

    public long policyRevision() {
        return policyRevision;
    }

    public WorldRulesSnapshot snapshot() {
        return new WorldRulesSnapshot(
                permanentDefinition,
                temporaryDefinition,
                temporaryExpiresAtGameTime,
                policyRevision
        );
    }

    public void applyPermanent(ClimbingRuleBookDefinition definition) {
        permanentDefinition = Optional.of(definition);
        clearTemporaryState();
        markPolicyChanged();
    }

    public void startTemporary(ClimbingRuleBookDefinition definition, long expiresAtGameTime) {
        temporaryDefinition = Optional.of(definition);
        temporaryExpiresAtGameTime = Math.max(0L, expiresAtGameTime);
        markPolicyChanged();
    }

    public void refreshTemporary(ClimbingRuleBookDefinition definition, long expiresAtGameTime) {
        temporaryDefinition = Optional.of(definition);
        temporaryExpiresAtGameTime = Math.max(0L, expiresAtGameTime);
        markPolicyChanged();
    }

    public boolean expireTemporaryIfDue(long currentGameTime) {
        if (temporaryDefinition.isEmpty() || currentGameTime < temporaryExpiresAtGameTime) {
            return false;
        }
        clearTemporaryState();
        markPolicyChanged();
        return true;
    }

    public void restoreDefaults() {
        permanentDefinition = Optional.empty();
        clearTemporaryState();
        markPolicyChanged();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putLong(POLICY_REVISION_KEY, policyRevision);
        permanentDefinition.ifPresent(definition -> encodeDefinition(definition, PERMANENT_DEFINITION_KEY, tag));
        temporaryDefinition.ifPresent(definition -> encodeDefinition(definition, TEMPORARY_DEFINITION_KEY, tag));
        if (temporaryDefinition.isPresent()) {
            tag.putLong(TEMPORARY_EXPIRES_AT_KEY, temporaryExpiresAtGameTime);
        }
        return tag;
    }

    private void clearTemporaryState() {
        temporaryDefinition = Optional.empty();
        temporaryExpiresAtGameTime = 0L;
    }

    private void markPolicyChanged() {
        rebuildRuntimeView();
        policyRevision = nextRevision(policyRevision);
        setDirty();
    }

    private void rebuildRuntimeView() {
        runtimeView = effectiveDefinition()
                .map(ClimbingRuleBookDefinition::profile)
                .map(ClimbingRulesRuntimeView::fromProfile)
                .orElseGet(ClimbingRulesRuntimeView::defaults);
    }

    private static void encodeDefinition(
            ClimbingRuleBookDefinition definition,
            String key,
            CompoundTag target
    ) {
        ClimbingRuleBookCodec.encodeToNbt(definition)
                .resultOrPartial(message -> LOGGER.error("Failed to save {}: {}", key, message))
                .ifPresent(definitionTag -> target.put(key, definitionTag));
    }

    private static ClimbingRulesSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        long revision = Math.max(0L, tag.getLong(POLICY_REVISION_KEY));
        Optional<ClimbingRuleBookDefinition> permanent = decodeWorldDefinition(
                tag,
                PERMANENT_DEFINITION_KEY,
                RuleBookActivationMode.PERMANENT
        );
        Optional<ClimbingRuleBookDefinition> temporary = decodeWorldDefinition(
                tag,
                TEMPORARY_DEFINITION_KEY,
                RuleBookActivationMode.TEMPORARY
        );
        long expiresAt = Math.max(0L, tag.getLong(TEMPORARY_EXPIRES_AT_KEY));

        if (permanent.isEmpty() && temporary.isEmpty() && tag.contains(LEGACY_ACTIVE_PROFILE_KEY)) {
            permanent = migrateLegacyProfile(tag.getCompound(LEGACY_ACTIVE_PROFILE_KEY));
        }
        if (temporary.isPresent() && expiresAt <= 0L) {
            LOGGER.error("Stored temporary WORLD rules have no valid expiry; temporary session discarded");
            temporary = Optional.empty();
        }
        if (temporary.isEmpty()) {
            expiresAt = 0L;
        }
        return new ClimbingRulesSavedData(permanent, temporary, expiresAt, revision);
    }

    private static Optional<ClimbingRuleBookDefinition> decodeWorldDefinition(
            CompoundTag root,
            String key,
            RuleBookActivationMode expectedActivation
    ) {
        Optional<ClimbingRuleBookDefinition> decoded = decodeDefinition(root, key);
        if (decoded.isEmpty()) {
            return Optional.empty();
        }
        ClimbingRuleBookDefinition definition = decoded.get();
        if (definition.activationMode() != expectedActivation || definition.scope() != RuleBookScope.WORLD) {
            LOGGER.error(
                    "Stored {} has invalid WORLD state metadata: activation={}, scope={}",
                    key,
                    definition.activationMode(),
                    definition.scope()
            );
            return Optional.empty();
        }
        return decoded;
    }

    private static Optional<ClimbingRuleBookDefinition> decodeDefinition(CompoundTag root, String key) {
        if (!root.contains(key)) {
            return Optional.empty();
        }
        Optional<ClimbingRuleBookDefinition> decoded = ClimbingRuleBookCodec.decodeFromNbt(root.getCompound(key))
                .resultOrPartial(message -> LOGGER.error("Failed to load {}: {}", key, message));
        if (decoded.isEmpty()) {
            return Optional.empty();
        }
        ClimbingRuleBookValidationResult validation = ClimbingRuleBookValidator.validateAndNormalize(decoded.get());
        if (!validation.valid()) {
            LOGGER.error("Stored {} contains {} validation issue(s)", key, validation.issues().size());
            return Optional.empty();
        }
        return Optional.of(validation.normalizedDefinition());
    }

    private static Optional<ClimbingRuleBookDefinition> migrateLegacyProfile(CompoundTag profileTag) {
        Optional<ClimbingRulesProfile> decoded = ClimbingRulesProfileCodec.decodeFromNbt(profileTag)
                .resultOrPartial(message -> LOGGER.error("Failed to migrate legacy world rules: {}", message));
        if (decoded.isEmpty()) {
            return Optional.empty();
        }
        ClimbingRulesValidationResult validation = ClimbingRulesValidator.validateAndNormalize(decoded.get());
        if (!validation.valid()) {
            return Optional.empty();
        }
        ClimbingRulesProfile profile = validation.normalizedProfile();
        return Optional.of(ClimbingRuleBookDefinition.permanentWorld(profile.profileName(), profile));
    }

    private static long nextRevision(long current) {
        return current == Long.MAX_VALUE ? 1L : current + 1L;
    }
}
