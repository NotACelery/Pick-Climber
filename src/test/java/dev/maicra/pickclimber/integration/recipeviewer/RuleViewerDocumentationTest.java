package dev.maicra.pickclimber.integration.recipeviewer;

import dev.maicra.pickclimber.rules.ClimbingRuleBookDefinition;
import dev.maicra.pickclimber.rules.RuleBookActivationMode;
import dev.maicra.pickclimber.rules.RuleBookScope;
import net.minecraft.world.item.DyeColor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuleViewerDocumentationTest {
    @Test
    void baseRuleBookUsesCurrentPortableSchema() {
        var definition = RuleViewerDocumentation.baseRuleBookDefinition();

        assertEquals(ClimbingRuleBookDefinition.CURRENT_FORMAT_VERSION, definition.formatVersion());
        assertEquals(DyeColor.WHITE, definition.coverColor());
        assertEquals(RuleBookActivationMode.PERMANENT, definition.activationMode());
        assertEquals(RuleBookScope.WORLD, definition.scope());
        assertEquals(0, definition.durationSeconds());
    }

    @Test
    void documentationRelationsKeepApplicationNonCrafting() {
        assertTrue(RuleViewerRelation.AUTHORING.hasOutput());
        assertFalse(RuleViewerRelation.APPLICATION.hasOutput());
    }
}
