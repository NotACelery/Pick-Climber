package dev.maicra.pickclimber.climb;

public enum AnchorIndicatorStatus {
    NONE("", 0xFFFFFF),
    READY("gui.pickclimber.anchor.ready", 0x55FF55),
    UNSTABLE("gui.pickclimber.anchor.unstable", 0x55FFFF),
    UNCLIMBABLE("gui.pickclimber.anchor.unclimbable", 0xFF5555),
    REQUIRES_STRONG_GRIP("gui.pickclimber.anchor.requires_strong_grip", 0xAA55FF),
    REQUIRES_STURDY_LATCH("gui.pickclimber.anchor.requires_sturdy_latch", 0x55FFFF),
    COOLDOWN("gui.pickclimber.anchor.cooldown", 0xAAAAAA),
    OUT_OF_RANGE("gui.pickclimber.anchor.out_of_range", 0xFFFF55),
    OBSTRUCTED("gui.pickclimber.anchor.obstructed", 0xFF7777);

    private final String translationKey;
    private final int color;

    AnchorIndicatorStatus(String translationKey, int color) {
        this.translationKey = translationKey;
        this.color = color;
    }

    public String translationKey() {
        return translationKey;
    }

    public int color() {
        return color;
    }
}
