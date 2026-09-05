package dev.maicra.pickclimber.client;

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

import dev.maicra.pickclimber.client.rules.ClimbingRulesExportScreen;
import dev.maicra.pickclimber.climb.AnchorIndicatorStatus;
import dev.maicra.pickclimber.rules.ClimbingRuleBookDefinition;
import dev.maicra.pickclimber.rules.ClimbingRulesClientState;
import dev.maicra.pickclimber.rules.DefaultRuleProfileFactory;

public final class PickClimberOptionsScreen extends Screen {
    private static final int CONTROL_HEIGHT = 20;
    private static final int GAP = 4;
    private static final int SIDE_MARGIN = 8;
    private static final int MIN_COLUMN_WIDTH = 190;
    private static final int MAX_COLUMN_WIDTH = 210;
    private static final int MIN_FOOTER_BUTTON_WIDTH = 120;
    private static final int PREVIEW_TOP = 38;
    private static final int PREVIEW_HEIGHT = 40;
    private static final int PREVIEW_PADDING = 4;
    private static final int PREVIEW_MAX_ICON_SIZE = PREVIEW_HEIGHT - PREVIEW_PADDING * 2;
    private static final int CONTROLS_TOP = 84;
    private static final int FOOTER_BOTTOM_MARGIN = 8;
    private static final int SCROLL_STEP = CONTROL_HEIGHT + GAP;

    private final Screen parent;
    private final List<OptionLayoutEntry> optionEntries = new ArrayList<>();

    private boolean twoColumnLayout;
    private int layoutLeft;
    private int layoutWidth;
    private int controlsBottom;
    private int visibleRows = 1;
    private int totalRows = 1;
    private int scrollRow;

    public PickClimberOptionsScreen(Screen parent) {
        super(Component.translatable("options.pickclimber.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        ClientPickClimberBootstrap.ensureInstalled();
        PickClimberClientOptions options = PickClimberClientOptionsStore.current();
        List<AbstractWidget> modDependentControls = new ArrayList<>();
        optionEntries.clear();

        Button interactions = toggleButton(
                "options.pickclimber.interactions",
                () -> PickClimberClientOptionsStore.current().interactionsEnabled(),
                value -> {
                    PickClimberClientOptionsStore.update(current -> current.withInteractionsEnabled(value));
                    ClientRuntimePreferenceController.requestImmediateSync(minecraft);
                    rebuildWidgets();
                }
        );
        registerOption(interactions, -1, 0, 0, true);

        Button indicatorMode = Button.builder(indicatorModeMessage(options), button -> {
            PickClimberClientOptionsStore.update(current -> current.withIndicatorMode(current.indicatorMode().next()));
            rebuildWidgets();
        }).bounds(0, 0, MAX_COLUMN_WIDTH, CONTROL_HEIGHT).build();
        registerOption(indicatorMode, 0, 1, 1, false);
        modDependentControls.add(indicatorMode);

        Button indicatorStyle = Button.builder(indicatorStyleMessage(options), button -> {
            PickClimberClientOptionsStore.update(
                    current -> current.withIndicatorStyle(current.indicatorStyle().next())
            );
            button.setMessage(indicatorStyleMessage(PickClimberClientOptionsStore.current()));
        }).bounds(0, 0, MAX_COLUMN_WIDTH, CONTROL_HEIGHT).build();
        registerOption(indicatorStyle, 1, 1, 4, false);
        modDependentControls.add(indicatorStyle);

        Button failureMessages = toggleButton(
                "options.pickclimber.failure_text",
                () -> PickClimberClientOptionsStore.current().showFailureText(),
                value -> {
                    PickClimberClientOptionsStore.update(current -> current.withShowFailureText(value));
                    ClientRuntimePreferenceController.requestImmediateSync(minecraft);
                }
        );
        registerOption(failureMessages, 0, 2, 2, false);
        modDependentControls.add(failureMessages);

        Button showBox = toggleButton(
                "options.pickclimber.show_box",
                () -> PickClimberClientOptionsStore.current().showIndicatorBox(),
                value -> {
                    PickClimberClientOptionsStore.update(current -> current.withShowIndicatorBox(value));
                    rebuildWidgets();
                }
        );
        registerOption(showBox, 1, 2, 5, false);
        modDependentControls.add(showBox);

        Button showUnclimbable = toggleButton(
                "options.pickclimber.show_unclimbable",
                () -> PickClimberClientOptionsStore.current().showUnclimbableIndicator(),
                value -> PickClimberClientOptionsStore.update(current -> current.withShowUnclimbableIndicator(value))
        );
        registerOption(showUnclimbable, 0, 3, 3, false);
        modDependentControls.add(showUnclimbable);

        DoubleOptionSlider boxTransparency = transparencySlider(
                options.boxTransparency(),
                "options.pickclimber.box_transparency",
                value -> PickClimberClientOptionsStore.update(current -> current.withBoxTransparency(value))
        );
        registerOption(boxTransparency, 1, 3, 9, false);
        modDependentControls.add(boxTransparency);

        DoubleOptionSlider iconScale = new DoubleOptionSlider(
                0,
                0,
                MAX_COLUMN_WIDTH,
                options.iconScale(),
                PickClimberClientOptions.MIN_ICON_SCALE,
                PickClimberClientOptions.MAX_ICON_SCALE,
                value -> percentMessage("options.pickclimber.icon_size", value),
                value -> PickClimberClientOptionsStore.update(current -> current.withIconScale(value))
        );
        registerOption(iconScale, 0, 4, 6, false);
        modDependentControls.add(iconScale);

        Button boxColor = intensityButton(
                "options.pickclimber.box_color_intensity",
                () -> PickClimberClientOptionsStore.current().boxColorIntensity(),
                value -> PickClimberClientOptionsStore.update(current -> current.withBoxColorIntensity(value))
        );
        registerOption(boxColor, 1, 4, 10, false);
        modDependentControls.add(boxColor);

        DoubleOptionSlider iconTransparency = transparencySlider(
                options.iconTransparency(),
                "options.pickclimber.icon_transparency",
                value -> PickClimberClientOptionsStore.update(current -> current.withIconTransparency(value))
        );
        registerOption(iconTransparency, 0, 5, 7, false);
        modDependentControls.add(iconTransparency);

        Button iconColor = intensityButton(
                "options.pickclimber.icon_color_intensity",
                () -> PickClimberClientOptionsStore.current().iconColorIntensity(),
                value -> PickClimberClientOptionsStore.update(current -> current.withIconColorIntensity(value))
        );
        registerOption(iconColor, 0, 6, 8, false);
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
        layoutOptions();
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
        renderScrollIndicator(gui);
    }

    @Override
    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double scrollX,
            double scrollY
    ) {
        if (scrollY != 0.0D
                && maxScrollRows() > 0
                && mouseY >= CONTROLS_TOP
                && mouseY < controlsBottom) {
            int steps = Math.max(1, (int) Math.round(Math.abs(scrollY)));
            int direction = scrollY > 0.0D ? -steps : steps;
            if (setScrollRow(scrollRow + direction)) {
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    private void renderPreviewPanel(GuiGraphics gui) {
        int panelWidth = Math.max(1, width - SIDE_MARGIN * 2);
        panelWidth = Math.min(MAX_COLUMN_WIDTH * 2 + GAP, panelWidth);
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

        Component status = Component.translatable("options.pickclimber.preview_status_ready");
        int statusWidth = font.width(status);
        int previewCenterY = PREVIEW_TOP + PREVIEW_HEIGHT / 2;
        int contentWidth = PREVIEW_MAX_ICON_SIZE + GAP * 2 + statusWidth;
        int contentLeft = Math.max(panelLeft + PREVIEW_PADDING, width / 2 - contentWidth / 2);
        int iconCenter = contentLeft + PREVIEW_MAX_ICON_SIZE / 2;
        int statusX = iconCenter + PREVIEW_MAX_ICON_SIZE / 2 + GAP * 2;

        AnchorIndicatorRenderer.renderPreview(
                gui,
                iconCenter,
                previewCenterY,
                PREVIEW_MAX_ICON_SIZE,
                AnchorIndicatorStatus.READY
        );
        gui.drawString(
                font,
                status,
                statusX,
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
        int availableWidth = Math.max(1, width - SIDE_MARGIN * 2);
        boolean stacked = availableWidth < MIN_FOOTER_BUTTON_WIDTH * 3 + GAP * 2;
        int footerRows = stacked ? 3 : 1;
        int footerHeight = footerRows * CONTROL_HEIGHT + (footerRows - 1) * GAP;
        int footerTop = height - FOOTER_BOTTOM_MARGIN - footerHeight;
        controlsBottom = Math.max(CONTROLS_TOP + CONTROL_HEIGHT, footerTop - GAP * 2);

        int footerWidth = Math.min(MAX_COLUMN_WIDTH * 2 + GAP, availableWidth);
        int startX = (width - footerWidth) / 2;
        Button resetDefaults;
        Button exportWorld;
        Button done;
        if (stacked) {
            resetDefaults = resetDefaultsButton(startX, footerTop, footerWidth);
            exportWorld = exportWorldRulesButton(startX, footerTop + CONTROL_HEIGHT + GAP, footerWidth);
            done = doneButton(startX, footerTop + (CONTROL_HEIGHT + GAP) * 2, footerWidth);
        } else {
            int buttonWidth = (footerWidth - GAP * 2) / 3;
            resetDefaults = resetDefaultsButton(startX, footerTop, buttonWidth);
            exportWorld = exportWorldRulesButton(startX + buttonWidth + GAP, footerTop, buttonWidth);
            done = doneButton(startX + (buttonWidth + GAP) * 2, footerTop, buttonWidth);
        }

        resetDefaults.active = interactionsEnabled;
        exportWorld.active = true;
        addRenderableWidget(resetDefaults);
        addRenderableWidget(exportWorld);
        addRenderableWidget(done);
    }

    private Button exportWorldRulesButton(int x, int y, int width) {
        return Button.builder(Component.translatable("options.pickclimber.export_world_rules"), button -> {
            ClimbingRuleBookDefinition definition = ClimbingRulesClientState.worldActiveDefinition()
                    .orElseGet(() -> {
                        String name = "Pick Climber Defaults";
                        return ClimbingRuleBookDefinition.permanentWorld(
                                name, DefaultRuleProfileFactory.create(name)
                        ).withAuthor("", "Pick Climber");
                    });
            minecraft.setScreen(new ClimbingRulesExportScreen(this, definition));
        }).bounds(x, y, width, CONTROL_HEIGHT).build();
    }

    private void layoutOptions() {
        int availableWidth = Math.max(1, width - SIDE_MARGIN * 2);
        twoColumnLayout = availableWidth >= MIN_COLUMN_WIDTH * 2 + GAP;

        int columnWidth;
        if (twoColumnLayout) {
            columnWidth = Math.min(MAX_COLUMN_WIDTH, (availableWidth - GAP) / 2);
            layoutWidth = columnWidth * 2 + GAP;
            totalRows = 7;
        } else {
            columnWidth = Math.min(MAX_COLUMN_WIDTH * 2 + GAP, availableWidth);
            layoutWidth = columnWidth;
            totalRows = optionEntries.size();
        }
        layoutLeft = (width - layoutWidth) / 2;

        int availableHeight = Math.max(CONTROL_HEIGHT, controlsBottom - CONTROLS_TOP);
        visibleRows = Math.max(1, (availableHeight + GAP) / SCROLL_STEP);
        visibleRows = Math.min(visibleRows, totalRows);
        scrollRow = clamp(scrollRow, 0, maxScrollRows());

        for (OptionLayoutEntry entry : optionEntries) {
            int row = twoColumnLayout ? entry.wideRow() : entry.narrowRow();
            boolean visible = row >= scrollRow && row < scrollRow + visibleRows;
            entry.widget().visible = visible;
            if (!visible) {
                continue;
            }

            int y = CONTROLS_TOP + (row - scrollRow) * SCROLL_STEP;
            if (!twoColumnLayout || entry.fullWidth()) {
                place(entry.widget(), layoutLeft, y, layoutWidth);
                continue;
            }

            int x = layoutLeft + entry.wideColumn() * (columnWidth + GAP);
            place(entry.widget(), x, y, columnWidth);
        }
    }

    private void renderScrollIndicator(GuiGraphics gui) {
        int maxScroll = maxScrollRows();
        if (maxScroll <= 0) {
            return;
        }

        int trackTop = CONTROLS_TOP;
        int trackHeight = Math.max(CONTROL_HEIGHT, controlsBottom - CONTROLS_TOP);
        int thumbHeight = Math.max(8, trackHeight * visibleRows / totalRows);
        int travel = Math.max(0, trackHeight - thumbHeight);
        int thumbTop = trackTop + travel * scrollRow / maxScroll;
        int x = Math.min(width - 3, layoutLeft + layoutWidth + 3);

        gui.fill(x, trackTop, x + 2, trackTop + trackHeight, 0x50303030);
        gui.fill(x, thumbTop, x + 2, thumbTop + thumbHeight, 0xFFC0C0C0);
    }

    private boolean setScrollRow(int next) {
        int clamped = clamp(next, 0, maxScrollRows());
        if (clamped == scrollRow) {
            return false;
        }
        scrollRow = clamped;
        layoutOptions();
        return true;
    }

    private int maxScrollRows() {
        return Math.max(0, totalRows - visibleRows);
    }

    private void registerOption(
            AbstractWidget widget,
            int wideColumn,
            int wideRow,
            int narrowRow,
            boolean fullWidth
    ) {
        optionEntries.add(new OptionLayoutEntry(widget, wideColumn, wideRow, narrowRow, fullWidth));
        addRenderableWidget(widget);
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
            double value,
            String translationKey,
            DoubleConsumer setter
    ) {
        return new DoubleOptionSlider(
                0,
                0,
                MAX_COLUMN_WIDTH,
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
        }).bounds(0, 0, MAX_COLUMN_WIDTH, CONTROL_HEIGHT).build();
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
        }).bounds(0, 0, MAX_COLUMN_WIDTH, CONTROL_HEIGHT).build();
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

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private record OptionLayoutEntry(
            AbstractWidget widget,
            int wideColumn,
            int wideRow,
            int narrowRow,
            boolean fullWidth
    ) {
    }
}
