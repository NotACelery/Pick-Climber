package dev.maicra.pickclimber.client;

import dev.maicra.pickclimber.climb.AnchorIndicatorStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.Supplier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class PickClimberOptionsScreen extends Screen {
    private static final int CONTROL_HEIGHT = 20;
    private static final int GAP = 4;
    private static final int COLUMN_WIDTH = 210;
    private static final int CONTENT_WIDTH = COLUMN_WIDTH * 2 + GAP;
    private static final int PREVIEW_TOP = 38;
    private static final int PREVIEW_HEIGHT = 40;
    private static final int PREVIEW_PADDING = 4;
    private static final int PREVIEW_MAX_ICON_SIZE = PREVIEW_HEIGHT - PREVIEW_PADDING * 2;
    private static final int CONTROLS_TOP = 84;

    private final Screen parent;

    public PickClimberOptionsScreen(Screen parent) {
        super(Component.translatable("options.pickclimber.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        ClientPickClimberBootstrap.ensureInstalled();
        PickClimberClientOptions options = PickClimberClientOptionsStore.current();
        List<AbstractWidget> modDependentControls = new ArrayList<>();

        int left = Math.max(8, (width - CONTENT_WIDTH) / 2);
        int right = left + COLUMN_WIDTH + GAP;
        int fullWidth = Math.min(CONTENT_WIDTH, width - 16);
        int fullLeft = (width - fullWidth) / 2;

        Button interactions = toggleButton(
                "options.pickclimber.interactions",
                () -> PickClimberClientOptionsStore.current().interactionsEnabled(),
                value -> {
                    PickClimberClientOptionsStore.update(current -> current.withInteractionsEnabled(value));
                    ClientRuntimePreferenceController.requestImmediateSync(minecraft);
                    rebuildWidgets();
                }
        );
        place(interactions, fullLeft, CONTROLS_TOP, fullWidth);
        addRenderableWidget(interactions);

        int row1 = CONTROLS_TOP + CONTROL_HEIGHT + GAP;
        Button indicatorMode = Button.builder(indicatorModeMessage(options), button -> {
            PickClimberClientOptionsStore.update(current -> current.withIndicatorMode(current.indicatorMode().next()));
            rebuildWidgets();
        }).bounds(left, row1, COLUMN_WIDTH, CONTROL_HEIGHT).build();
        addRenderableWidget(indicatorMode);
        modDependentControls.add(indicatorMode);

        Button indicatorStyle = Button.builder(indicatorStyleMessage(options), button -> {
            PickClimberClientOptionsStore.update(
                    current -> current.withIndicatorStyle(current.indicatorStyle().next())
            );
            button.setMessage(indicatorStyleMessage(PickClimberClientOptionsStore.current()));
        }).bounds(right, row1, COLUMN_WIDTH, CONTROL_HEIGHT).build();
        addRenderableWidget(indicatorStyle);
        modDependentControls.add(indicatorStyle);

        int row2 = row1 + CONTROL_HEIGHT + GAP;
        Button failureMessages = toggleButton(
                "options.pickclimber.failure_text",
                () -> PickClimberClientOptionsStore.current().showFailureText(),
                value -> {
                    PickClimberClientOptionsStore.update(current -> current.withShowFailureText(value));
                    ClientRuntimePreferenceController.requestImmediateSync(minecraft);
                }
        );
        place(failureMessages, left, row2, COLUMN_WIDTH);
        addRenderableWidget(failureMessages);
        modDependentControls.add(failureMessages);

        Button showBox = toggleButton(
                "options.pickclimber.show_box",
                () -> PickClimberClientOptionsStore.current().showIndicatorBox(),
                value -> {
                    PickClimberClientOptionsStore.update(current -> current.withShowIndicatorBox(value));
                    rebuildWidgets();
                }
        );
        place(showBox, right, row2, COLUMN_WIDTH);
        addRenderableWidget(showBox);
        modDependentControls.add(showBox);

        int row3 = row2 + CONTROL_HEIGHT + GAP;
        Button showUnclimbable = toggleButton(
                "options.pickclimber.show_unclimbable",
                () -> PickClimberClientOptionsStore.current().showUnclimbableIndicator(),
                value -> PickClimberClientOptionsStore.update(current -> current.withShowUnclimbableIndicator(value))
        );
        place(showUnclimbable, left, row3, COLUMN_WIDTH);
        addRenderableWidget(showUnclimbable);
        modDependentControls.add(showUnclimbable);

        DoubleOptionSlider boxTransparency = transparencySlider(
                right,
                row3,
                options.boxTransparency(),
                "options.pickclimber.box_transparency",
                value -> PickClimberClientOptionsStore.update(current -> current.withBoxTransparency(value))
        );
        addRenderableWidget(boxTransparency);
        modDependentControls.add(boxTransparency);

        int row4 = row3 + CONTROL_HEIGHT + GAP;
        DoubleOptionSlider iconScale = new DoubleOptionSlider(
                left,
                row4,
                COLUMN_WIDTH,
                options.iconScale(),
                PickClimberClientOptions.MIN_ICON_SCALE,
                PickClimberClientOptions.MAX_ICON_SCALE,
                value -> percentMessage("options.pickclimber.icon_size", value),
                value -> PickClimberClientOptionsStore.update(current -> current.withIconScale(value))
        );
        addRenderableWidget(iconScale);
        modDependentControls.add(iconScale);

        Button boxColor = intensityButton(
                "options.pickclimber.box_color_intensity",
                () -> PickClimberClientOptionsStore.current().boxColorIntensity(),
                value -> PickClimberClientOptionsStore.update(current -> current.withBoxColorIntensity(value))
        );
        place(boxColor, right, row4, COLUMN_WIDTH);
        addRenderableWidget(boxColor);
        modDependentControls.add(boxColor);

        int row5 = row4 + CONTROL_HEIGHT + GAP;
        DoubleOptionSlider iconTransparency = transparencySlider(
                left,
                row5,
                options.iconTransparency(),
                "options.pickclimber.icon_transparency",
                value -> PickClimberClientOptionsStore.update(current -> current.withIconTransparency(value))
        );
        addRenderableWidget(iconTransparency);
        modDependentControls.add(iconTransparency);

        int row6 = row5 + CONTROL_HEIGHT + GAP;
        Button iconColor = intensityButton(
                "options.pickclimber.icon_color_intensity",
                () -> PickClimberClientOptionsStore.current().iconColorIntensity(),
                value -> PickClimberClientOptionsStore.update(current -> current.withIconColorIntensity(value))
        );
        place(iconColor, left, row6, COLUMN_WIDTH);
        addRenderableWidget(iconColor);
        modDependentControls.add(iconColor);

        applyAvailability(
                options,
                modDependentControls,
                indicatorStyle,
                showUnclimbable,
                iconScale,
                iconTransparency,
                iconColor,
                showBox,
                boxTransparency,
                boxColor
        );
        addFooterControls(options.interactionsEnabled());
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        renderBackground(gui, mouseX, mouseY, partialTick);
        super.render(gui, mouseX, mouseY, partialTick);
        gui.drawCenteredString(font, title, width / 2, 12, 0xFFFFFF);
        gui.drawCenteredString(
                font,
                Component.translatable("options.pickclimber.subtitle"),
                width / 2,
                25,
                0xB0B0B0
        );
        renderPreviewPanel(gui);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    private void renderPreviewPanel(GuiGraphics gui) {
        int panelWidth = Math.min(CONTENT_WIDTH, width - 16);
        int panelLeft = (width - panelWidth) / 2;
        gui.fill(panelLeft, PREVIEW_TOP, panelLeft + panelWidth, PREVIEW_TOP + PREVIEW_HEIGHT, 0xE0101010);
        gui.fill(panelLeft, PREVIEW_TOP, panelLeft + panelWidth, PREVIEW_TOP + 1, 0xFF606060);
        gui.fill(
                panelLeft,
                PREVIEW_TOP + PREVIEW_HEIGHT - 1,
                panelLeft + panelWidth,
                PREVIEW_TOP + PREVIEW_HEIGHT,
                0xFF303030
        );

        PickClimberClientOptions options = PickClimberClientOptionsStore.current();
        if (!options.interactionsEnabled()) {
            drawPreviewMessage(gui, "options.pickclimber.preview_disabled", 0x909090);
            return;
        }
        if (options.indicatorMode() == IndicatorMode.OFF) {
            drawPreviewMessage(gui, "options.pickclimber.preview_indicator_off", 0x909090);
            return;
        }

        int previewCenterY = PREVIEW_TOP + PREVIEW_HEIGHT / 2;
        int iconCenter = width / 2 - 46;
        AnchorIndicatorRenderer.renderPreview(
                gui,
                iconCenter,
                previewCenterY,
                PREVIEW_MAX_ICON_SIZE,
                AnchorIndicatorStatus.READY
        );
        gui.drawString(
                font,
                Component.translatable("options.pickclimber.preview_status_ready"),
                width / 2 - 20,
                PREVIEW_TOP + (PREVIEW_HEIGHT - font.lineHeight) / 2,
                0xFFFFFF
        );
    }

    private void drawPreviewMessage(GuiGraphics gui, String translationKey, int color) {
        gui.drawCenteredString(
                font,
                Component.translatable(translationKey),
                width / 2,
                PREVIEW_TOP + (PREVIEW_HEIGHT - font.lineHeight) / 2,
                color
        );
    }

    private void addFooterControls(boolean interactionsEnabled) {
        int buttonWidth = Math.min(200, Math.max(120, (width - 24) / 2));
        int totalWidth = buttonWidth * 2 + GAP;
        int startX = (width - totalWidth) / 2;
        int y = height - 28;
        Button resetDefaults = resetDefaultsButton(startX, y, buttonWidth);
        resetDefaults.active = interactionsEnabled;
        addRenderableWidget(resetDefaults);
        addRenderableWidget(doneButton(startX + buttonWidth + GAP, y, buttonWidth));
    }

    private Button resetDefaultsButton(int x, int y, int buttonWidth) {
        return Button.builder(
                Component.translatable("options.pickclimber.reset_defaults"),
                button -> {
                    PickClimberClientOptionsStore.resetToDefaults();
                    ClientRuntimePreferenceController.requestImmediateSync(minecraft);
                    rebuildWidgets();
                }
        ).bounds(x, y, buttonWidth, CONTROL_HEIGHT).build();
    }

    private Button doneButton(int x, int y, int buttonWidth) {
        return Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(x, y, buttonWidth, CONTROL_HEIGHT)
                .build();
    }

    private static void applyAvailability(
            PickClimberClientOptions options,
            List<AbstractWidget> modDependentControls,
            AbstractWidget indicatorStyle,
            AbstractWidget showUnclimbable,
            AbstractWidget iconScale,
            AbstractWidget iconTransparency,
            AbstractWidget iconColor,
            AbstractWidget showBox,
            AbstractWidget boxTransparency,
            AbstractWidget boxColor
    ) {
        boolean enabled = options.interactionsEnabled();
        modDependentControls.forEach(widget -> widget.active = enabled);
        if (!enabled) {
            return;
        }

        boolean indicatorVisible = options.indicatorMode() != IndicatorMode.OFF;
        indicatorStyle.active = indicatorVisible;
        iconScale.active = indicatorVisible;
        iconTransparency.active = indicatorVisible;
        iconColor.active = indicatorVisible;
        showBox.active = indicatorVisible;
        boxTransparency.active = indicatorVisible && options.showIndicatorBox();
        boxColor.active = indicatorVisible && options.showIndicatorBox();
        showUnclimbable.active = options.indicatorMode() == IndicatorMode.CONTEXTUAL;
    }

    private static DoubleOptionSlider transparencySlider(
            int x,
            int y,
            double value,
            String translationKey,
            DoubleConsumer setter
    ) {
        return new DoubleOptionSlider(
                x,
                y,
                COLUMN_WIDTH,
                value,
                0.0D,
                1.0D,
                sliderValue -> percentMessage(translationKey, sliderValue),
                setter
        );
    }

    private static Button toggleButton(
            String translationKey,
            BooleanSupplier valueSupplier,
            Consumer<Boolean> setter
    ) {
        return Button.builder(toggleMessage(translationKey, valueSupplier.getAsBoolean()), button -> {
            boolean next = !valueSupplier.getAsBoolean();
            setter.accept(next);
            button.setMessage(toggleMessage(translationKey, next));
        }).bounds(0, 0, COLUMN_WIDTH, CONTROL_HEIGHT).build();
    }

    private static Button intensityButton(
            String translationKey,
            Supplier<IndicatorColorIntensity> valueSupplier,
            Consumer<IndicatorColorIntensity> setter
    ) {
        return Button.builder(intensityMessage(translationKey, valueSupplier.get()), button -> {
            IndicatorColorIntensity next = valueSupplier.get().next();
            setter.accept(next);
            button.setMessage(intensityMessage(translationKey, next));
        }).bounds(0, 0, COLUMN_WIDTH, CONTROL_HEIGHT).build();
    }

    private static void place(AbstractWidget widget, int x, int y, int widgetWidth) {
        widget.setX(x);
        widget.setY(y);
        widget.setWidth(widgetWidth);
    }

    private static Component indicatorModeMessage(PickClimberClientOptions options) {
        return Component.translatable("options.pickclimber.indicator_mode", options.indicatorMode().label());
    }

    private static Component indicatorStyleMessage(PickClimberClientOptions options) {
        return Component.translatable("options.pickclimber.indicator_style", options.indicatorStyle().label());
    }

    private static Component intensityMessage(String translationKey, IndicatorColorIntensity intensity) {
        return Component.translatable(translationKey, intensity.label());
    }

    private static Component toggleMessage(String translationKey, boolean enabled) {
        Component state = Component.translatable(enabled ? "options.on" : "options.off");
        return Component.translatable(translationKey, state);
    }

    private static Component percentMessage(String translationKey, double value) {
        return Component.translatable(translationKey, Math.round(value * 100.0D) + "%");
    }
}
