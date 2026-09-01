package dev.maicra.pickclimber.client;

import net.minecraft.network.chat.Component;

public enum IndicatorStyle {
    STRING("options.pickclimber.indicator_style.string"),
    PICKAXE("options.pickclimber.indicator_style.pickaxe");

    private final String translationKey;

    IndicatorStyle(String translationKey) {
        this.translationKey = translationKey;
    }

    public Component label() {
        return Component.translatable(translationKey);
    }

    public IndicatorStyle next() {
        IndicatorStyle[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}
