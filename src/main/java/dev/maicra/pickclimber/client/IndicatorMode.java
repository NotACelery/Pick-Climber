package dev.maicra.pickclimber.client;

import net.minecraft.network.chat.Component;

public enum IndicatorMode {
    CONTEXTUAL("options.pickclimber.indicator_mode.contextual"),
    ALWAYS("options.pickclimber.indicator_mode.always"),
    OFF("options.pickclimber.indicator_mode.off");

    private final String translationKey;

    IndicatorMode(String translationKey) {
        this.translationKey = translationKey;
    }

    public Component label() {
        return Component.translatable(translationKey);
    }

    public IndicatorMode next() {
        IndicatorMode[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}
