package dev.maicra.pickclimber.rules;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class RuleDefinitionIdTest {
    @Test
    void gameplayIdentityIgnoresDisplayName() {
        ClimbingRulesProfile first = profile("Route A", "minecraft:stone");
        ClimbingRulesProfile renamed = profile("Route B", "minecraft:stone");

        assertEquals(RuleDefinitionId.of(first), RuleDefinitionId.of(renamed));
    }

    @Test
    void gameplayIdentityChangesWhenRulesChange() {
        ClimbingRulesProfile first = profile("Route", "minecraft:stone");
        ClimbingRulesProfile changed = profile("Route", "minecraft:deepslate");

        assertNotEquals(RuleDefinitionId.of(first), RuleDefinitionId.of(changed));
    }

    @Test
    void unknownBlockIdsRemainPartOfPortableIdentity() {
        ClimbingRulesProfile first = profile("Portable", "missing_mod:wall");
        ClimbingRulesProfile second = profile("Portable", "missing_mod:other_wall");

        assertNotEquals(RuleDefinitionId.of(first), RuleDefinitionId.of(second));
    }

    private static ResourceLocation resource(String value) {
        String[] parts = value.split(":", 2);
        return ResourceLocation.fromNamespaceAndPath(parts[0], parts[1]);
    }

    private static ClimbingRulesProfile profile(String name, String stableId) {
        return new ClimbingRulesProfile(
                ClimbingRulesProfile.CURRENT_FORMAT_VERSION,
                name,
                Set.of(resource(stableId)),
                Set.of(),
                Set.of(),
                UnlistedPolicy.UNCLIMBABLE,
                ClimbingRulesProfile.DEFAULT_PICKAXE_WEAR,
                true,
                false
        );
    }
}
