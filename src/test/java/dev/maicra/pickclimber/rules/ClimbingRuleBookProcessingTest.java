package dev.maicra.pickclimber.rules;

import java.util.Set;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class ClimbingRuleBookProcessingTest {
    @Test
    void recolorChangesOnlyPortableCoverColor() {
        ClimbingRulesProfile profile = new ClimbingRulesProfile(
                ClimbingRulesProfile.CURRENT_FORMAT_VERSION,
                "Red Route",
                Set.of(ResourceLocation.fromNamespaceAndPath("minecraft", "stone")),
                Set.of(ResourceLocation.fromNamespaceAndPath("minecraft", "sand")),
                Set.of(ResourceLocation.fromNamespaceAndPath("minecraft", "bedrock")),
                UnlistedPolicy.USE_PICK_CLIMBER_DEFAULTS,
                17,
                false,
                true
        );
        ClimbingRuleBookDefinition source = new ClimbingRuleBookDefinition(
                ClimbingRuleBookDefinition.CURRENT_FORMAT_VERSION,
                "Red Route",
                DyeColor.BLUE,
                profile,
                RuleBookActivationMode.TEMPORARY,
                RuleBookScope.PLAYER,
                75
        );

        ClimbingRuleBookDefinition recolored = ClimbingRuleBookProcessing.recolor(source, DyeColor.RED);

        assertEquals(DyeColor.RED, recolored.coverColor());
        assertEquals(source.bookName(), recolored.bookName());
        assertEquals(source.profile(), recolored.profile());
        assertEquals(source.activationMode(), recolored.activationMode());
        assertEquals(source.scope(), recolored.scope());
        assertEquals(source.durationSeconds(), recolored.durationSeconds());
    }

    @Test
    void sameColorIsANoOp() {
        ClimbingRuleBookDefinition source = ClimbingRuleBookDefinition.permanentWorld(
                "White",
                ClimbingRulesProfile.defaults("White")
        );

        assertSame(source, ClimbingRuleBookProcessing.recolor(source, DyeColor.WHITE));
    }
}
