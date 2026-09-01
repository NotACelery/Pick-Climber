package dev.maicra.pickclimber.client;

import net.minecraft.network.chat.Component;

public enum IndicatorColorIntensity {
    MUTED("options.pickclimber.color_intensity.muted"),
    NORMAL("options.pickclimber.color_intensity.normal"),
    NEON("options.pickclimber.color_intensity.neon");

    private final String translationKey;

    IndicatorColorIntensity(String translationKey) {
        this.translationKey = translationKey;
    }

    public Component label() {
        return Component.translatable(translationKey);
    }

    public IndicatorColorIntensity next() {
        IndicatorColorIntensity[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}
