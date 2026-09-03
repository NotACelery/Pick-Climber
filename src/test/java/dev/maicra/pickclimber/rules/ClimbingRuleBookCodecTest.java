package dev.maicra.pickclimber.rules;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClimbingRuleBookCodecTest {
    @Test
    void roundTripPreservesPortableIdentity() {
        ClimbingRulesProfile profile = new ClimbingRulesProfile(
                ClimbingRulesProfile.CURRENT_FORMAT_VERSION,
                "Timed route",
                Set.of(ResourceLocation.fromNamespaceAndPath("missing_mod", "route_block")),
                Set.of(),
                Set.of(ResourceLocation.fromNamespaceAndPath("minecraft", "bedrock")),
                UnlistedPolicy.USE_PICK_CLIMBER_DEFAULTS,
                75,
                false,
                true
        );
        ClimbingRuleBookDefinition source = new ClimbingRuleBookDefinition(
                ClimbingRuleBookDefinition.CURRENT_FORMAT_VERSION,
                "Timed route",
                DyeColor.BLUE,
                profile,
                RuleBookActivationMode.TEMPORARY,
                RuleBookScope.PLAYER,
                90
        );

        var encoded = ClimbingRuleBookCodec.encodeToJson(source).getOrThrow();
        var decoded = ClimbingRuleBookCodec.decodeFromJson(encoded).getOrThrow();

        assertEquals(source, decoded);
    }

    @Test
    void intMaxTemporaryDurationRoundTripsThroughPortableCodec() {
        ClimbingRuleBookDefinition source = new ClimbingRuleBookDefinition(
                ClimbingRuleBookDefinition.CURRENT_FORMAT_VERSION,
                "Long Route",
                DyeColor.WHITE,
                ClimbingRulesProfile.defaults("Long Route"),
                RuleBookActivationMode.TEMPORARY,
                RuleBookScope.WORLD,
                Integer.MAX_VALUE
        );

        var encoded = ClimbingRuleBookCodec.encodeToJson(source).getOrThrow();
        var decoded = ClimbingRuleBookCodec.decodeFromJson(encoded).getOrThrow();

        assertEquals(Integer.MAX_VALUE, decoded.durationSeconds());
    }
}
