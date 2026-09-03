package dev.maicra.pickclimber.rules;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClimbingRulesRuntimeViewTest {
    @Test
    void defaultViewMatchesTheOnePointOneBaselineContract() {
        ClimbingRulesRuntimeView view = ClimbingRulesRuntimeView.defaults();

        assertFalse(view.active());
        assertEquals(UnlistedPolicy.USE_PICK_CLIMBER_DEFAULTS, view.unlistedPolicy());
        assertEquals(100, view.durabilityMultiplierPercent());
        assertTrue(view.playerMiningEnabled());
        assertFalse(view.unmineableTerminals());
    }

    @Test
    void explicitOverridesAndGlobalRulesAreCompiledIntoRuntimeView() {
        ResourceLocation stable = ResourceLocation.fromNamespaceAndPath("minecraft", "stone");
        ResourceLocation unstable = ResourceLocation.fromNamespaceAndPath("minecraft", "sand");
        ResourceLocation blocked = ResourceLocation.fromNamespaceAndPath("minecraft", "bedrock");
        ClimbingRulesProfile profile = new ClimbingRulesProfile(
                1,
                "Runtime",
                Set.of(stable),
                Set.of(unstable),
                Set.of(blocked),
                UnlistedPolicy.UNCLIMBABLE,
                50,
                false,
                true
        );

        ClimbingRulesRuntimeView view = ClimbingRulesRuntimeView.fromProfile(profile);

        assertTrue(view.active());
        assertEquals(SurfaceClassification.STABLE, view.classificationOverride(stable).orElseThrow());
        assertEquals(SurfaceClassification.UNSTABLE, view.classificationOverride(unstable).orElseThrow());
        assertEquals(SurfaceClassification.UNCLIMBABLE, view.classificationOverride(blocked).orElseThrow());
        assertEquals(UnlistedPolicy.UNCLIMBABLE, view.unlistedPolicy());
        assertEquals(50, view.durabilityMultiplierPercent());
        assertFalse(view.playerMiningEnabled());
        assertTrue(view.unmineableTerminals());
    }
}
