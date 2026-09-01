package dev.maicra.pickclimber.client;

public record PickClimberClientOptions(
        IndicatorMode indicatorMode,
        IndicatorStyle indicatorStyle,
        boolean showUnclimbableIndicator,
        double iconScale,
        double iconTransparency,
        IndicatorColorIntensity iconColorIntensity,
        boolean showIndicatorBox,
        double boxTransparency,
        IndicatorColorIntensity boxColorIntensity,
        boolean showFailureText,
        boolean interactionsEnabled
) {
    public static final double MIN_ICON_SCALE = 0.5D;
    public static final double MAX_ICON_SCALE = 2.0D;

    public PickClimberClientOptions {
        indicatorMode = indicatorMode == null ? IndicatorMode.CONTEXTUAL : indicatorMode;
        indicatorStyle = indicatorStyle == null ? IndicatorStyle.STRING : indicatorStyle;
        iconColorIntensity = iconColorIntensity == null ? IndicatorColorIntensity.NORMAL : iconColorIntensity;
        boxColorIntensity = boxColorIntensity == null ? IndicatorColorIntensity.NORMAL : boxColorIntensity;
        iconScale = clamp(iconScale, MIN_ICON_SCALE, MAX_ICON_SCALE);
        iconTransparency = clamp(iconTransparency, 0.0D, 1.0D);
        boxTransparency = clamp(boxTransparency, 0.0D, 1.0D);
    }

    public static PickClimberClientOptions defaults() {
        return new PickClimberClientOptions(
                IndicatorMode.CONTEXTUAL,
                IndicatorStyle.STRING,
                false,
                1.0D,
                0.0D,
                IndicatorColorIntensity.NORMAL,
                true,
                0.0D,
                IndicatorColorIntensity.NORMAL,
                true,
                true
        );
    }

    public PickClimberClientOptions resetToDefaults() {
        return defaults();
    }

    public PickClimberClientOptions withIndicatorMode(IndicatorMode value) {
        return copy(value, indicatorStyle, showUnclimbableIndicator, iconScale, iconTransparency,
                iconColorIntensity, showIndicatorBox, boxTransparency, boxColorIntensity,
                showFailureText, interactionsEnabled);
    }

    public PickClimberClientOptions withIndicatorStyle(IndicatorStyle value) {
        return copy(indicatorMode, value, showUnclimbableIndicator, iconScale, iconTransparency,
                iconColorIntensity, showIndicatorBox, boxTransparency, boxColorIntensity,
                showFailureText, interactionsEnabled);
    }

    public PickClimberClientOptions withShowUnclimbableIndicator(boolean value) {
        return copy(indicatorMode, indicatorStyle, value, iconScale, iconTransparency,
                iconColorIntensity, showIndicatorBox, boxTransparency, boxColorIntensity,
                showFailureText, interactionsEnabled);
    }

    public PickClimberClientOptions withIconScale(double value) {
        return copy(indicatorMode, indicatorStyle, showUnclimbableIndicator, value, iconTransparency,
                iconColorIntensity, showIndicatorBox, boxTransparency, boxColorIntensity,
                showFailureText, interactionsEnabled);
    }

    public PickClimberClientOptions withIconTransparency(double value) {
        return copy(indicatorMode, indicatorStyle, showUnclimbableIndicator, iconScale, value,
                iconColorIntensity, showIndicatorBox, boxTransparency, boxColorIntensity,
                showFailureText, interactionsEnabled);
    }

    public PickClimberClientOptions withIconColorIntensity(IndicatorColorIntensity value) {
        return copy(indicatorMode, indicatorStyle, showUnclimbableIndicator, iconScale, iconTransparency,
                value, showIndicatorBox, boxTransparency, boxColorIntensity,
                showFailureText, interactionsEnabled);
    }

    public PickClimberClientOptions withShowIndicatorBox(boolean value) {
        return copy(indicatorMode, indicatorStyle, showUnclimbableIndicator, iconScale, iconTransparency,
                iconColorIntensity, value, boxTransparency, boxColorIntensity,
                showFailureText, interactionsEnabled);
    }

    public PickClimberClientOptions withBoxTransparency(double value) {
        return copy(indicatorMode, indicatorStyle, showUnclimbableIndicator, iconScale, iconTransparency,
                iconColorIntensity, showIndicatorBox, value, boxColorIntensity,
                showFailureText, interactionsEnabled);
    }

    public PickClimberClientOptions withBoxColorIntensity(IndicatorColorIntensity value) {
        return copy(indicatorMode, indicatorStyle, showUnclimbableIndicator, iconScale, iconTransparency,
                iconColorIntensity, showIndicatorBox, boxTransparency, value,
                showFailureText, interactionsEnabled);
    }

    public PickClimberClientOptions withShowFailureText(boolean value) {
        return copy(indicatorMode, indicatorStyle, showUnclimbableIndicator, iconScale, iconTransparency,
                iconColorIntensity, showIndicatorBox, boxTransparency, boxColorIntensity,
                value, interactionsEnabled);
    }

    public PickClimberClientOptions withInteractionsEnabled(boolean value) {
        return copy(indicatorMode, indicatorStyle, showUnclimbableIndicator, iconScale, iconTransparency,
                iconColorIntensity, showIndicatorBox, boxTransparency, boxColorIntensity,
                showFailureText, value);
    }

    private static PickClimberClientOptions copy(
            IndicatorMode indicatorMode,
            IndicatorStyle indicatorStyle,
            boolean showUnclimbableIndicator,
            double iconScale,
            double iconTransparency,
            IndicatorColorIntensity iconColorIntensity,
            boolean showIndicatorBox,
            double boxTransparency,
            IndicatorColorIntensity boxColorIntensity,
            boolean showFailureText,
            boolean interactionsEnabled
    ) {
        return new PickClimberClientOptions(
                indicatorMode,
                indicatorStyle,
                showUnclimbableIndicator,
                iconScale,
                iconTransparency,
                iconColorIntensity,
                showIndicatorBox,
                boxTransparency,
                boxColorIntensity,
                showFailureText,
                interactionsEnabled
        );
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
