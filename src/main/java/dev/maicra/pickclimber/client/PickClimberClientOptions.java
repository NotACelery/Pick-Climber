package dev.maicra.pickclimber.client;

public record PickClimberClientOptions(
        IndicatorMode indicatorMode,
        IndicatorStyle indicatorStyle,
        boolean showUnclimbableIndicator,
        double iconScale,
        double iconOpacity,
        boolean showIndicatorBox,
        double boxOpacity,
        boolean showFailureText,
        boolean interactionsEnabled
) {
    public static final double MIN_ICON_SCALE = 0.5D;
    public static final double MAX_ICON_SCALE = 2.0D;

    public PickClimberClientOptions {
        indicatorMode = indicatorMode == null ? IndicatorMode.CONTEXTUAL : indicatorMode;
        indicatorStyle = indicatorStyle == null ? IndicatorStyle.STRING : indicatorStyle;
        iconScale = clamp(iconScale, MIN_ICON_SCALE, MAX_ICON_SCALE);
        iconOpacity = clamp(iconOpacity, 0.0D, 1.0D);
        boxOpacity = clamp(boxOpacity, 0.0D, 1.0D);
    }

    public static PickClimberClientOptions defaults() {
        return new PickClimberClientOptions(
                IndicatorMode.CONTEXTUAL,
                IndicatorStyle.STRING,
                false,
                1.0D,
                1.0D,
                true,
                1.0D,
                true,
                true
        );
    }

    public PickClimberClientOptions resetHud() {
        PickClimberClientOptions defaults = defaults();
        return new PickClimberClientOptions(
                defaults.indicatorMode,
                defaults.indicatorStyle,
                defaults.showUnclimbableIndicator,
                defaults.iconScale,
                defaults.iconOpacity,
                defaults.showIndicatorBox,
                defaults.boxOpacity,
                defaults.showFailureText,
                interactionsEnabled
        );
    }

    public PickClimberClientOptions resetAll() {
        return defaults();
    }

    public PickClimberClientOptions withIndicatorMode(IndicatorMode value) {
        return copy(value, indicatorStyle, showUnclimbableIndicator, iconScale, iconOpacity,
                showIndicatorBox, boxOpacity, showFailureText, interactionsEnabled);
    }

    public PickClimberClientOptions withIndicatorStyle(IndicatorStyle value) {
        return copy(indicatorMode, value, showUnclimbableIndicator, iconScale, iconOpacity,
                showIndicatorBox, boxOpacity, showFailureText, interactionsEnabled);
    }

    public PickClimberClientOptions withShowUnclimbableIndicator(boolean value) {
        return copy(indicatorMode, indicatorStyle, value, iconScale, iconOpacity,
                showIndicatorBox, boxOpacity, showFailureText, interactionsEnabled);
    }

    public PickClimberClientOptions withIconScale(double value) {
        return copy(indicatorMode, indicatorStyle, showUnclimbableIndicator, value, iconOpacity,
                showIndicatorBox, boxOpacity, showFailureText, interactionsEnabled);
    }

    public PickClimberClientOptions withIconOpacity(double value) {
        return copy(indicatorMode, indicatorStyle, showUnclimbableIndicator, iconScale, value,
                showIndicatorBox, boxOpacity, showFailureText, interactionsEnabled);
    }

    public PickClimberClientOptions withShowIndicatorBox(boolean value) {
        return copy(indicatorMode, indicatorStyle, showUnclimbableIndicator, iconScale, iconOpacity,
                value, boxOpacity, showFailureText, interactionsEnabled);
    }

    public PickClimberClientOptions withBoxOpacity(double value) {
        return copy(indicatorMode, indicatorStyle, showUnclimbableIndicator, iconScale, iconOpacity,
                showIndicatorBox, value, showFailureText, interactionsEnabled);
    }

    public PickClimberClientOptions withShowFailureText(boolean value) {
        return copy(indicatorMode, indicatorStyle, showUnclimbableIndicator, iconScale, iconOpacity,
                showIndicatorBox, boxOpacity, value, interactionsEnabled);
    }

    public PickClimberClientOptions withInteractionsEnabled(boolean value) {
        return copy(indicatorMode, indicatorStyle, showUnclimbableIndicator, iconScale, iconOpacity,
                showIndicatorBox, boxOpacity, showFailureText, value);
    }

    private static PickClimberClientOptions copy(
            IndicatorMode indicatorMode,
            IndicatorStyle indicatorStyle,
            boolean showUnclimbableIndicator,
            double iconScale,
            double iconOpacity,
            boolean showIndicatorBox,
            double boxOpacity,
            boolean showFailureText,
            boolean interactionsEnabled
    ) {
        return new PickClimberClientOptions(
                indicatorMode,
                indicatorStyle,
                showUnclimbableIndicator,
                iconScale,
                iconOpacity,
                showIndicatorBox,
                boxOpacity,
                showFailureText,
                interactionsEnabled
        );
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
