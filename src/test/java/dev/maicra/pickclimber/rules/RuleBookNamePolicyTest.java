package dev.maicra.pickclimber.rules;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuleBookNamePolicyTest {
    @Test
    void portableNamesRemainExactFileStems() {
        assertEquals("Route A", RuleBookNamePolicy.portableFileStem("Route A").orElseThrow());
        assertEquals("parkour.route", RuleBookNamePolicy.portableFileStem("parkour.route").orElseThrow());
    }

    @Test
    void crossPlatformUnsafeNamesAreRejected() {
        assertFalse(RuleBookNamePolicy.portableFileStem("route/main").isPresent());
        assertFalse(RuleBookNamePolicy.portableFileStem("route:main").isPresent());
        assertFalse(RuleBookNamePolicy.portableFileStem("CON").isPresent());
        assertFalse(RuleBookNamePolicy.portableFileStem("COM1.backup").isPresent());
        assertFalse(RuleBookNamePolicy.portableFileStem("route.").isPresent());
    }

    @Test
    void legacyFilenameNormalizationProducesPortableStem() {
        assertEquals("route_main", RuleBookNamePolicy.normalizeImportedFileStem("route/main.json"));
        assertTrue(RuleBookNamePolicy.portableFileStem(
                RuleBookNamePolicy.normalizeImportedFileStem("route/main.json")
        ).isPresent());
    }
}
