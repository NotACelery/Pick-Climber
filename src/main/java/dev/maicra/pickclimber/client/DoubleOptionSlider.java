package dev.maicra.pickclimber.client;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

final class DoubleOptionSlider extends AbstractSliderButton {
    private final double min;
    private final double max;
    private final DoubleFunction<Component> messageFactory;
    private final DoubleConsumer onChanged;

    DoubleOptionSlider(
            int x,
            int y,
            int width,
            double initialValue,
            double min,
            double max,
            DoubleFunction<Component> messageFactory,
            DoubleConsumer onChanged
    ) {
        super(x, y, width, 20, Component.empty(), normalized(initialValue, min, max));
        this.min = min;
        this.max = max;
        this.messageFactory = messageFactory;
        this.onChanged = onChanged;
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        setMessage(messageFactory.apply(actualValue()));
    }

    @Override
    protected void applyValue() {
        onChanged.accept(actualValue());
    }

    private double actualValue() {
        return min + value * (max - min);
    }

    private static double normalized(double value, double min, double max) {
        if (max <= min) {
            return 0.0D;
        }
        return Math.max(0.0D, Math.min(1.0D, (value - min) / (max - min)));
    }
}
