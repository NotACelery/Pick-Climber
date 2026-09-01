package dev.maicra.pickclimber.client;

import dev.maicra.pickclimber.climb.AnchorIndicatorStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class PickClimberOptionsScreen extends Screen {
    private static final int CONTROL_HEIGHT = 20;
    private static final int CONTROL_GAP = 4;
    private static final int MIN_CONTROL_WIDTH = 136;
    private static final int MAX_CONTROL_WIDTH = 210;
    private static final int SIDE_MARGIN = 10;
    private static final int TOP_MARGIN = 48;
    private static final int FOOTER_HEIGHT = 76;

    private final Screen parent;

    public PickClimberOptionsScreen(Screen parent) {
        super(Component.translatable("options.pickclimber.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        ClientPickClimberBootstrap.ensureInstalled();
        PickClimberClientOptions options = PickClimberClientOptionsStore.current();
        List<AbstractWidget> controls = new ArrayList<>();

        Button indicatorMode = Button.builder(indicatorModeMessage(options), button -> {
            PickClimberClientOptionsStore.update(current -> current.withIndicatorMode(current.indicatorMode().next()));
            rebuildWidgets();
        }).bounds(0, 0, MAX_CONTROL_WIDTH, CONTROL_HEIGHT).build();
        controls.add(indicatorMode);

        Button indicatorStyle = Button.builder(indicatorStyleMessage(options), button -> {
            PickClimberClientOptionsStore.update(
                    current -> current.withIndicatorStyle(current.indicatorStyle().next())
            );
            button.setMessage(indicatorStyleMessage(PickClimberClientOptionsStore.current()));
        }).bounds(0, 0, MAX_CONTROL_WIDTH, CONTROL_HEIGHT).build();
        controls.add(indicatorStyle);

        Button showUnclimbable = toggleButton(
                "options.pickclimber.show_unclimbable",
                () -> PickClimberClientOptionsStore.current().showUnclimbableIndicator(),
                value -> PickClimberClientOptionsStore.update(current -> current.withShowUnclimbableIndicator(value))
        );
        controls.add(showUnclimbable);

        DoubleOptionSlider iconScale = new DoubleOptionSlider(
                0,
                0,
                MAX_CONTROL_WIDTH,
                options.iconScale(),
                PickClimberClientOptions.MIN_ICON_SCALE,
                PickClimberClientOptions.MAX_ICON_SCALE,
                value -> percentMessage("options.pickclimber.icon_size", value),
                value -> PickClimberClientOptionsStore.update(current -> current.withIconScale(value))
        );
        controls.add(iconScale);

        DoubleOptionSlider iconOpacity = new DoubleOptionSlider(
                0,
                0,
                MAX_CONTROL_WIDTH,
                options.iconOpacity(),
                0.0D,
                1.0D,
                value -> percentMessage("options.pickclimber.icon_opacity", value),
                value -> PickClimberClientOptionsStore.update(current -> current.withIconOpacity(value))
        );
        controls.add(iconOpacity);

        Button showBox = toggleButton(
                "options.pickclimber.show_box",
                () -> PickClimberClientOptionsStore.current().showIndicatorBox(),
                value -> {
                    PickClimberClientOptionsStore.update(current -> current.withShowIndicatorBox(value));
                    rebuildWidgets();
                }
        );
        controls.add(showBox);

        DoubleOptionSlider boxOpacity = new DoubleOptionSlider(
                0,
                0,
                MAX_CONTROL_WIDTH,
                options.boxOpacity(),
                0.0D,
                1.0D,
                value -> percentMessage("options.pickclimber.box_opacity", value),
                value -> PickClimberClientOptionsStore.update(current -> current.withBoxOpacity(value))
        );
        controls.add(boxOpacity);

        controls.add(toggleButton(
                "options.pickclimber.failure_text",
                () -> PickClimberClientOptionsStore.current().showFailureText(),
                value -> {
                    PickClimberClientOptionsStore.update(current -> current.withShowFailureText(value));
                    ClientRuntimePreferenceController.requestImmediateSync(minecraft);
                }
        ));

        controls.add(toggleButton(
                "options.pickclimber.interactions",
                () -> PickClimberClientOptionsStore.current().interactionsEnabled(),
                value -> {
                    PickClimberClientOptionsStore.update(current -> current.withInteractionsEnabled(value));
                    ClientRuntimePreferenceController.requestImmediateSync(minecraft);
                }
        ));

        applyControlAvailability(
                options,
                indicatorStyle,
                showUnclimbable,
                iconScale,
                iconOpacity,
                showBox,
                boxOpacity
        );
        layoutControls(controls);
        controls.forEach(this::addRenderableWidget);
        addFooterControls();
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        renderBackground(gui, mouseX, mouseY, partialTick);
        gui.drawCenteredString(font, title, width / 2, 16, 0xFFFFFF);
        gui.drawCenteredString(
                font,
                Component.translatable("options.pickclimber.subtitle"),
                width / 2,
                30,
                0xA0A0A0
        );
        renderIndicatorPreview(gui);
        super.render(gui, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    private void renderIndicatorPreview(GuiGraphics gui) {
        PickClimberClientOptions options = PickClimberClientOptionsStore.current();
        if (width < 300 || options.indicatorMode() == IndicatorMode.OFF) {
            return;
        }
        int centerX = width - 36;
        gui.drawCenteredString(
                font,
                Component.translatable("options.pickclimber.preview"),
                centerX,
                6,
                0xA0A0A0
        );
        AnchorIndicatorRenderer.renderPreview(gui, centerX, 18, AnchorIndicatorStatus.READY);
    }

    private void addFooterControls() {
        int buttonWidth = Math.min(140, Math.max(90, (width - 32) / 3));
        int totalWidth = buttonWidth * 3 + CONTROL_GAP * 2;
        if (totalWidth <= width - SIDE_MARGIN * 2) {
            int startX = (width - totalWidth) / 2;
            int y = height - 48;
            addRenderableWidget(resetHudButton(startX, y, buttonWidth));
            addRenderableWidget(resetAllButton(startX + buttonWidth + CONTROL_GAP, y, buttonWidth));
            addRenderableWidget(doneButton(startX + (buttonWidth + CONTROL_GAP) * 2, y, buttonWidth));
            return;
        }

        int rowWidth = Math.min(150, Math.max(90, (width - 24) / 2));
        int topY = height - 68;
        addRenderableWidget(resetHudButton(width / 2 - rowWidth - 3, topY, rowWidth));
        addRenderableWidget(resetAllButton(width / 2 + 3, topY, rowWidth));
        addRenderableWidget(doneButton(width / 2 - rowWidth / 2, topY + CONTROL_HEIGHT + CONTROL_GAP, rowWidth));
    }

    private Button resetHudButton(int x, int y, int buttonWidth) {
        return Button.builder(
                Component.translatable("options.pickclimber.reset_hud"),
                button -> {
                    PickClimberClientOptionsStore.resetHud();
                    ClientRuntimePreferenceController.requestImmediateSync(minecraft);
                    rebuildWidgets();
                }
        ).bounds(x, y, buttonWidth, CONTROL_HEIGHT).build();
    }

    private Button resetAllButton(int x, int y, int buttonWidth) {
        return Button.builder(
                Component.translatable("options.pickclimber.reset_all"),
                button -> {
                    PickClimberClientOptionsStore.resetAll();
                    ClientRuntimePreferenceController.requestImmediateSync(minecraft);
                    rebuildWidgets();
                }
        ).bounds(x, y, buttonWidth, CONTROL_HEIGHT).build();
    }

    private Button doneButton(int x, int y, int buttonWidth) {
        return Button.builder(
                Component.translatable("gui.done"),
                button -> onClose()
        ).bounds(x, y, buttonWidth, CONTROL_HEIGHT).build();
    }

    private void layoutControls(List<AbstractWidget> controls) {
        int availableWidth = Math.max(MIN_CONTROL_WIDTH, width - SIDE_MARGIN * 2);
        int availableHeight = Math.max(CONTROL_HEIGHT, height - TOP_MARGIN - FOOTER_HEIGHT);
        int rowsPerColumn = Math.max(1, (availableHeight + CONTROL_GAP) / (CONTROL_HEIGHT + CONTROL_GAP));
        int minimumColumns = (int) Math.ceil(controls.size() / (double) rowsPerColumn);
        int maximumColumns = Math.max(1, availableWidth / (MIN_CONTROL_WIDTH + CONTROL_GAP));
        int columns = Math.max(1, Math.min(Math.max(1, minimumColumns), maximumColumns));
        int rows = (int) Math.ceil(controls.size() / (double) columns);
        int controlWidth = Math.min(
                MAX_CONTROL_WIDTH,
                Math.max(MIN_CONTROL_WIDTH, (availableWidth - (columns - 1) * CONTROL_GAP) / columns)
        );
        int totalWidth = columns * controlWidth + (columns - 1) * CONTROL_GAP;
        int startX = Math.max(SIDE_MARGIN, (width - totalWidth) / 2);
        int totalHeight = rows * CONTROL_HEIGHT + (rows - 1) * CONTROL_GAP;
        int startY = TOP_MARGIN + Math.max(0, (availableHeight - totalHeight) / 2);

        for (int index = 0; index < controls.size(); index++) {
            int column = index / rows;
            int row = index % rows;
            AbstractWidget widget = controls.get(index);
            widget.setX(startX + column * (controlWidth + CONTROL_GAP));
            widget.setY(startY + row * (CONTROL_HEIGHT + CONTROL_GAP));
            widget.setWidth(controlWidth);
        }
    }

    private static void applyControlAvailability(
            PickClimberClientOptions options,
            AbstractWidget indicatorStyle,
            AbstractWidget showUnclimbable,
            AbstractWidget iconScale,
            AbstractWidget iconOpacity,
            AbstractWidget showBox,
            AbstractWidget boxOpacity
    ) {
        boolean indicatorVisible = options.indicatorMode() != IndicatorMode.OFF;
        indicatorStyle.active = indicatorVisible;
        iconScale.active = indicatorVisible;
        iconOpacity.active = indicatorVisible;
        showBox.active = indicatorVisible;
        boxOpacity.active = indicatorVisible && options.showIndicatorBox();
        showUnclimbable.active = options.indicatorMode() == IndicatorMode.CONTEXTUAL;
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
        }).bounds(0, 0, MAX_CONTROL_WIDTH, CONTROL_HEIGHT).build();
    }

    private static Component indicatorModeMessage(PickClimberClientOptions options) {
        return Component.translatable("options.pickclimber.indicator_mode", options.indicatorMode().label());
    }

    private static Component indicatorStyleMessage(PickClimberClientOptions options) {
        return Component.translatable("options.pickclimber.indicator_style", options.indicatorStyle().label());
    }

    private static Component toggleMessage(String translationKey, boolean enabled) {
        Component state = Component.translatable(enabled ? "options.on" : "options.off");
        return Component.translatable(translationKey, state);
    }

    private static Component percentMessage(String translationKey, double value) {
        return Component.translatable(translationKey, Math.round(value * 100.0D) + "%");
    }
}
