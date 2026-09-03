package dev.maicra.pickclimber.climb;

final class ToolWearMath {
    private ToolWearMath() {
    }

    static Result scale(int baseAmount, int multiplierPercent, int previousRemainder) {
        int safeBaseAmount = Math.max(0, baseAmount);
        int safeMultiplier = Math.max(0, Math.min(500, multiplierPercent));
        int safeRemainder = Math.max(0, Math.min(99, previousRemainder));
        long scaledHundredths = (long) safeBaseAmount * safeMultiplier + safeRemainder;
        int damage = (int) Math.min(Integer.MAX_VALUE, scaledHundredths / 100L);
        int remainder = (int) (scaledHundredths % 100L);
        return new Result(damage, remainder);
    }

    record Result(int damage, int remainder) {
    }
}
