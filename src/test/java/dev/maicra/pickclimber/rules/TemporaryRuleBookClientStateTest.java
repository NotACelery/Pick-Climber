package dev.maicra.pickclimber.rules;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TemporaryRuleBookClientStateTest {
    @AfterEach
    void clearState() {
        TemporaryRuleBookClientState.clear();
    }

    @Test
    void negativeExpiryIsNormalizedToInactive() {
        TemporaryRuleBookClientState.apply(-5L);
        assertEquals(0L, TemporaryRuleBookClientState.expiresAtGameTime());
    }

    @Test
    void applyAndClearTrackTransportExpiry() {
        TemporaryRuleBookClientState.apply(1_234L);
        assertEquals(1_234L, TemporaryRuleBookClientState.expiresAtGameTime());

        TemporaryRuleBookClientState.clear();
        assertEquals(0L, TemporaryRuleBookClientState.expiresAtGameTime());
    }
}
