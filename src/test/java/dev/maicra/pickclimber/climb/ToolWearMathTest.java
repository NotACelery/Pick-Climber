package dev.maicra.pickclimber.climb;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ToolWearMathTest {
    @Test
    void fiftyPercentAccumulatesOnePointAcrossTwoSinglePointEvents() {
        ToolWearMath.Result first = ToolWearMath.scale(1, 50, 0);
        ToolWearMath.Result second = ToolWearMath.scale(1, 50, first.remainder());

        assertEquals(0, first.damage());
        assertEquals(50, first.remainder());
        assertEquals(1, second.damage());
        assertEquals(0, second.remainder());
    }

    @Test
    void oneHundredFiftyPercentAlternatesWithoutLosingFractions() {
        ToolWearMath.Result first = ToolWearMath.scale(1, 150, 0);
        ToolWearMath.Result second = ToolWearMath.scale(1, 150, first.remainder());

        assertEquals(1, first.damage());
        assertEquals(50, first.remainder());
        assertEquals(2, second.damage());
        assertEquals(0, second.remainder());
    }

    @Test
    void zeroPercentNeverSpendsLogicalWear() {
        ToolWearMath.Result result = ToolWearMath.scale(20, 0, 75);

        assertEquals(0, result.damage());
        assertEquals(75, result.remainder());
    }

    @Test
    void twoHundredPercentIsExact() {
        ToolWearMath.Result result = ToolWearMath.scale(15, 200, 0);

        assertEquals(30, result.damage());
        assertEquals(0, result.remainder());
    }

    @Test
    void fiveHundredPercentIsExactAtMaximum() {
        ToolWearMath.Result result = ToolWearMath.scale(20, 500, 0);

        assertEquals(100, result.damage());
        assertEquals(0, result.remainder());
    }
}
