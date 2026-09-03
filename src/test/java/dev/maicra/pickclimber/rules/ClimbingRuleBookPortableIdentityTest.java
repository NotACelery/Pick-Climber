package dev.maicra.pickclimber.rules;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ClimbingRuleBookPortableIdentityTest {
    @Test
    void identicalPortableDefinitionsEncodeIdentically() {
        ClimbingRuleBookDefinition first = baseDefinition();
        ClimbingRuleBookDefinition second = baseDefinition();

        assertEquals(canonicalTag(first), canonicalTag(second));
    }

    @Test
    void everyPortableIdentityFieldChangesCanonicalData() {
        ClimbingRuleBookDefinition base = baseDefinition();

        assertNotEquals(canonicalTag(base), canonicalTag(withName(base, "Other Route")));
        assertNotEquals(canonicalTag(base), canonicalTag(withColor(base, DyeColor.RED)));
        assertNotEquals(canonicalTag(base), canonicalTag(withDuration(base, 121)));
        assertNotEquals(canonicalTag(base), canonicalTag(withScope(base, RuleBookScope.WORLD)));
        assertNotEquals(canonicalTag(base), canonicalTag(asPermanent(base)));
        assertNotEquals(canonicalTag(base), canonicalTag(withDifferentRules(base)));
        assertNotEquals(canonicalTag(base), canonicalTag(withDifferentMissingId(base)));
    }

    private static ClimbingRuleBookDefinition baseDefinition() {
        String name = "Identity Route";
        ClimbingRulesProfile profile = new ClimbingRulesProfile(
                ClimbingRulesProfile.CURRENT_FORMAT_VERSION,
                name,
                Set.of(ResourceLocation.fromNamespaceAndPath("missing_mod", "stable")),
                Set.of(),
                Set.of(ResourceLocation.fromNamespaceAndPath("minecraft", "bedrock")),
                UnlistedPolicy.USE_PICK_CLIMBER_DEFAULTS,
                100,
                true,
                false
        );
        return new ClimbingRuleBookDefinition(
                ClimbingRuleBookDefinition.CURRENT_FORMAT_VERSION,
                name,
                DyeColor.BLUE,
                profile,
                RuleBookActivationMode.TEMPORARY,
                RuleBookScope.PLAYER,
                120
        );
    }

    private static CompoundTag canonicalTag(ClimbingRuleBookDefinition definition) {
        ClimbingRuleBookValidationResult validation = ClimbingRuleBookValidator.validateAndNormalize(definition);
        return ClimbingRuleBookCodec.encodeToNbt(validation.normalizedDefinition()).getOrThrow();
    }

    private static ClimbingRuleBookDefinition withName(
            ClimbingRuleBookDefinition source,
            String name
    ) {
        ClimbingRulesProfile profile = source.profile();
        ClimbingRulesProfile renamed = new ClimbingRulesProfile(
                profile.formatVersion(),
                name,
                profile.stableBlocks(),
                profile.unstableBlocks(),
                profile.unclimbableBlocks(),
                profile.unlistedPolicy(),
                profile.durabilityMultiplierPercent(),
                profile.playerMiningEnabled(),
                profile.unmineableTerminals()
        );
        return new ClimbingRuleBookDefinition(
                source.formatVersion(),
                name,
                source.coverColor(),
                renamed,
                source.activationMode(),
                source.scope(),
                source.durationSeconds()
        );
    }

    private static ClimbingRuleBookDefinition withColor(
            ClimbingRuleBookDefinition source,
            DyeColor color
    ) {
        return copy(source, source.profile(), color, source.activationMode(), source.scope(), source.durationSeconds());
    }

    private static ClimbingRuleBookDefinition withDuration(
            ClimbingRuleBookDefinition source,
            int duration
    ) {
        return copy(source, source.profile(), source.coverColor(), source.activationMode(), source.scope(), duration);
    }

    private static ClimbingRuleBookDefinition withScope(
            ClimbingRuleBookDefinition source,
            RuleBookScope scope
    ) {
        return copy(
                source,
                source.profile(),
                source.coverColor(),
                source.activationMode(),
                scope,
                source.durationSeconds()
        );
    }

    private static ClimbingRuleBookDefinition asPermanent(ClimbingRuleBookDefinition source) {
        return copy(
                source,
                source.profile(),
                source.coverColor(),
                RuleBookActivationMode.PERMANENT,
                RuleBookScope.WORLD,
                0
        );
    }

    private static ClimbingRuleBookDefinition withDifferentRules(ClimbingRuleBookDefinition source) {
        ClimbingRulesProfile profile = source.profile();
        ClimbingRulesProfile changed = new ClimbingRulesProfile(
                profile.formatVersion(),
                profile.profileName(),
                profile.stableBlocks(),
                Set.of(ResourceLocation.fromNamespaceAndPath("minecraft", "sand")),
                profile.unclimbableBlocks(),
                profile.unlistedPolicy(),
                profile.durabilityMultiplierPercent(),
                profile.playerMiningEnabled(),
                profile.unmineableTerminals()
        );
        return copy(
                source,
                changed,
                source.coverColor(),
                source.activationMode(),
                source.scope(),
                source.durationSeconds()
        );
    }

    private static ClimbingRuleBookDefinition withDifferentMissingId(ClimbingRuleBookDefinition source) {
        ClimbingRulesProfile profile = source.profile();
        ClimbingRulesProfile changed = new ClimbingRulesProfile(
                profile.formatVersion(),
                profile.profileName(),
                Set.of(ResourceLocation.fromNamespaceAndPath("missing_mod", "other_stable")),
                profile.unstableBlocks(),
                profile.unclimbableBlocks(),
                profile.unlistedPolicy(),
                profile.durabilityMultiplierPercent(),
                profile.playerMiningEnabled(),
                profile.unmineableTerminals()
        );
        return copy(
                source,
                changed,
                source.coverColor(),
                source.activationMode(),
                source.scope(),
                source.durationSeconds()
        );
    }

    private static ClimbingRuleBookDefinition copy(
            ClimbingRuleBookDefinition source,
            ClimbingRulesProfile profile,
            DyeColor color,
            RuleBookActivationMode activation,
            RuleBookScope scope,
            int duration
    ) {
        return new ClimbingRuleBookDefinition(
                source.formatVersion(),
                source.bookName(),
                color,
                profile,
                activation,
                scope,
                duration
        );
    }
}
