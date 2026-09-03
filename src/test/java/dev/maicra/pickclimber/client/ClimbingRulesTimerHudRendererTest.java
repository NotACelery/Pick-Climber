package dev.maicra.pickclimber.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClimbingRulesTimerHudRendererTest {
    @Test
    void countdownRoundsUpPartialSeconds() {
        assertEquals("0:01", ClimbingRulesTimerHudRenderer.formatRemaining(1L));
        assertEquals("0:02", ClimbingRulesTimerHudRenderer.formatRemaining(21L));
    }

    @Test
    void countdownFormatsMinutesAndHours() {
        assertEquals("1:00", ClimbingRulesTimerHudRenderer.formatRemaining(1_200L));
        assertEquals("1:01:01", ClimbingRulesTimerHudRenderer.formatRemaining(73_220L));
    }
}
