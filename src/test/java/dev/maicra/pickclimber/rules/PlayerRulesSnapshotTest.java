package dev.maicra.pickclimber.rules;

import java.util.Optional;

import net.minecraft.world.item.DyeColor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerRulesSnapshotTest {
    @Test
    void inactiveSnapshotHasNoPortableState() {
        PlayerRulesSnapshot snapshot = PlayerRulesSnapshot.inactive();

        assertFalse(snapshot.active());
        assertTrue(snapshot.definition().isEmpty());
        assertEquals(0L, snapshot.expiresAtGameTime());
        assertEquals(0L, snapshot.policyRevision());
    }

    @Test
    void activeSnapshotKeepsAbsoluteExpiryAndRevision() {
        ClimbingRuleBookDefinition definition = new ClimbingRuleBookDefinition(
                ClimbingRuleBookDefinition.CURRENT_FORMAT_VERSION,
                "Player route",
                DyeColor.WHITE,
                ClimbingRulesProfile.defaults("Player route"),
                RuleBookActivationMode.TEMPORARY,
                RuleBookScope.PLAYER,
                30
        );
        PlayerRulesSnapshot snapshot = new PlayerRulesSnapshot(Optional.of(definition), 12_345L, 98L);

        assertTrue(snapshot.active());
        assertEquals(12_345L, snapshot.expiresAtGameTime());
        assertEquals(98L, snapshot.policyRevision());
    }
}
