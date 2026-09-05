package dev.maicra.pickclimber.client.rules;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.IntConsumer;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;

import dev.maicra.pickclimber.rules.ClimbingRuleBookCodec;
import dev.maicra.pickclimber.rules.ClimbingRuleBookDefinition;
import dev.maicra.pickclimber.rules.ClimbingRulesProfile;
import dev.maicra.pickclimber.rules.RuleBookActivationMode;
import dev.maicra.pickclimber.rules.RuleBookScope;
import dev.maicra.pickclimber.rules.SurfaceClassification;
import dev.maicra.pickclimber.rules.UnlistedPolicy;

public final class ClimbingRulesEditorScreen extends Screen {
    private static final int CELL = 22;
    private static final int COLUMNS = 10;
    private static final int GRID_WIDTH = COLUMNS * CELL;
    private static final int GRID_TOP = 98;
    private static final int SIDE_WIDTH = 240;
    private static final int PANEL_GAP = 18;
    private static final int MIN_WIDE_WIDTH = 520;
    private static final int MIN_WIDE_HEIGHT = 330;
    private static final int NARROW_SCROLL_STEP = 24;
    private static final int NARROW_VIEW_TOP = 20;
    private static final int NARROW_BOTTOM_MARGIN = 8;
    private static final int SCROLLBAR_WIDTH = 6;

    private final Screen parent;
    private final BlockPos tablePosition;
    private final int sessionToken;
    private final Set<ResourceLocation> stable;
    private final Set<ResourceLocation> unstable;
    private final Set<ResourceLocation> unclimbable;
    private final Set<ResourceLocation> selected = new LinkedHashSet<>();
    private final Set<ResourceLocation> clearedToUnclimbable = new LinkedHashSet<>();
    private final List<BlockCatalogService.Entry> catalog;
    private final Set<ResourceLocation> missingIds;

    private EditBox searchBox;
    private EditBox profileName;
    private Button allTabButton;
    private Button stableTabButton;
    private Button unstableTabButton;
    private Button unclimbableTabButton;
    private Button stableAssignButton;
    private Button unstableAssignButton;
    private Button unclimbableAssignButton;
    private Button miningButton;
    private Button terminalsButton;
    private Button activationButton;
    private Button scopeButton;
    private WearSlider wearSlider;
    private EditBox durationField;
    private ViewTab tab = ViewTab.ALL;
    private SurfaceClassification assignmentMode;
    private int pickaxeWear;
    private boolean playerMiningEnabled;
    private boolean unmineableTerminals;
    private final DyeColor coverColor;
    private RuleBookActivationMode activationMode;
    private RuleBookScope scope;
    private int durationSeconds;
    private String draftProfileName;
    private String searchQuery = "";
    private int scrollRow;
    private List<BlockCatalogService.Entry> filtered = List.of();

    private boolean wideLayout;
    private int gridLeft;
    private int gridTop;
    private int visibleRows;
    private int actionTop;
    private int sideLeft;
    private int sideTop;
    private int narrowScrollOffset;
    private int narrowContentHeight;
    private boolean scrollbarDragging;
    private boolean narrowScrollbarDragging;

    public ClimbingRulesEditorScreen(
            Screen parent,
            BlockPos tablePosition,
            int sessionToken,
            ClimbingRuleBookDefinition definition
    ) {
        super(Component.translatable("gui.pickclimber.rules.editor_title"));
        this.parent = parent;
        this.tablePosition = tablePosition;
        this.sessionToken = sessionToken;
        ClimbingRulesProfile profile = definition.profile();
        stable = new LinkedHashSet<>(profile.stableBlocks());
        unstable = new LinkedHashSet<>(profile.unstableBlocks());
        unclimbable = new LinkedHashSet<>(profile.unclimbableBlocks());
        draftProfileName = definition.bookName();
        pickaxeWear = profile.pickaxeWear();
        playerMiningEnabled = profile.playerMiningEnabled();
        unmineableTerminals = profile.unmineableTerminals();
        coverColor = definition.coverColor();
        activationMode = definition.activationMode();
        scope = definition.scope();
        durationSeconds = definition.durationSeconds();
        catalog = BlockCatalogService.entries();
        missingIds = findMissingIds(profile);
    }

    @Override
    protected void init() {
        calculateLayout();
        int contentOffset = wideLayout ? 0 : -narrowScrollOffset;

        profileName = new EditBox(font, gridLeft, 26 + contentOffset, GRID_WIDTH, 18, Component.empty());
        profileName.setMaxLength(ClimbingRulesProfile.MAX_PROFILE_NAME_LENGTH);
        profileName.setValue(draftProfileName);
        profileName.setResponder(value -> draftProfileName = value);
        addRenderableWidget(profileName);

        searchBox = new EditBox(font, gridLeft, 50 + contentOffset, GRID_WIDTH, 18, Component.empty());
        searchBox.setHint(Component.translatable("gui.pickclimber.rules.search"));
        searchBox.setValue(searchQuery);
        searchBox.setResponder(value -> {
            searchQuery = value;
            rebuildFilter();
        });
        addRenderableWidget(searchBox);

        int tabsTop = 74 + contentOffset;
        int tabsLeft = gridLeft - 14;
        allTabButton = addRenderableWidget(tabButton(
                ViewTab.ALL,
                tabsLeft,
                tabsTop,
                42,
                "gui.pickclimber.rules.all"
        ));
        stableTabButton = addRenderableWidget(tabButton(
                ViewTab.STABLE,
                tabsLeft + 46,
                tabsTop,
                48,
                "gui.pickclimber.rules.stable"
        ));
        unstableTabButton = addRenderableWidget(tabButton(
                ViewTab.UNSTABLE,
                tabsLeft + 98,
                tabsTop,
                56,
                "gui.pickclimber.rules.unstable"
        ));
        unclimbableTabButton = addRenderableWidget(tabButton(
                ViewTab.UNCLIMBABLE,
                tabsLeft + 158,
                tabsTop,
                70,
                "gui.pickclimber.rules.unclimbable"
        ));
        refreshTabButtons();

        addSelectionControls();
        addRulesControls();
        rebuildFilter(false);
        clampScroll();
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        renderBackground(gui, mouseX, mouseY, partialTick);
        if (!wideLayout) {
            gui.enableScissor(
                    0,
                    NARROW_VIEW_TOP,
                    width,
                    Math.max(NARROW_VIEW_TOP + 1, height - NARROW_BOTTOM_MARGIN)
            );
        }
        super.render(gui, mouseX, mouseY, partialTick);
        renderGrid(gui, mouseX, mouseY);
        renderScrollbar(gui);
        renderPanelLabels(gui);
        renderAssignmentButtonBorders(gui);
        renderTabIndicator(gui);
        renderNarrowScrollbar(gui);
        if (!wideLayout) {
            gui.disableScissor();
        }
        gui.drawCenteredString(font, title, width / 2, 8, 0xFFFFFF);
        renderStatus(gui);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && clickNarrowScrollbar(mouseX, mouseY)) {
            return true;
        }
        if (button == 0 && clickScrollbar(mouseX, mouseY)) {
            return true;
        }
        if (button == 0 && clickGrid(mouseX, mouseY)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0 && narrowScrollbarDragging) {
            updateNarrowScrollFromMouse(mouseY);
            return true;
        }
        if (button == 0 && scrollbarDragging) {
            updateScrollFromMouse(mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && narrowScrollbarDragging) {
            narrowScrollbarDragging = false;
            return true;
        }
        if (button == 0 && scrollbarDragging) {
            scrollbarDragging = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        boolean overGrid = mouseX >= gridLeft
                && mouseX < gridLeft + GRID_WIDTH + SCROLLBAR_WIDTH + 4
                && mouseY >= gridTop
                && mouseY < gridTop + gridHeight();
        if (overGrid && scrollY != 0.0D) {
            int direction = scrollY > 0.0D ? -1 : 1;
            int previous = scrollRow;
            scrollRow = Math.max(0, Math.min(maxScrollRows(), scrollRow + direction));
            return previous != scrollRow;
        }
        if (!wideLayout && scrollY != 0.0D) {
            int direction = scrollY > 0.0D ? -NARROW_SCROLL_STEP : NARROW_SCROLL_STEP;
            int previous = narrowScrollOffset;
            narrowScrollOffset = Math.max(0, Math.min(maxNarrowScrollOffset(), narrowScrollOffset + direction));
            if (previous != narrowScrollOffset) {
                rebuildWidgets();
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    public Screen parentScreen() {
        return parent;
    }

    private void calculateLayout() {
        wideLayout = width >= MIN_WIDE_WIDTH && height >= MIN_WIDE_HEIGHT;
        if (wideLayout) {
            narrowScrollOffset = 0;
            int totalWidth = GRID_WIDTH + PANEL_GAP + SIDE_WIDTH;
            gridLeft = Math.max(8, (width - totalWidth) / 2);
            gridTop = GRID_TOP;
            visibleRows = Math.max(3, Math.min(8, (height - GRID_TOP - 88) / CELL));
            sideLeft = gridLeft + GRID_WIDTH + PANEL_GAP;
            sideTop = 60;
            actionTop = gridTop + gridHeight() + 6;
            narrowContentHeight = 0;
            return;
        }

        gridLeft = Math.max(8, (width - GRID_WIDTH) / 2);
        visibleRows = 4;
        int baseActionTop = GRID_TOP + gridHeight() + 6;
        int baseSideTop = baseActionTop + 78;
        narrowContentHeight = baseSideTop + 246;
        narrowScrollOffset = Math.max(0, Math.min(maxNarrowScrollOffset(), narrowScrollOffset));
        gridTop = GRID_TOP - narrowScrollOffset;
        actionTop = baseActionTop - narrowScrollOffset;
        sideLeft = gridLeft;
        sideTop = baseSideTop - narrowScrollOffset;
    }

    private void addSelectionControls() {
        addRenderableWidget(Button.builder(Component.translatable("gui.pickclimber.rules.select_visible"), button ->
                selectVisiblePage()
        ).bounds(gridLeft, actionTop, 98, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.pickclimber.rules.clear_selection"), button -> {
            selected.clear();
            assignmentMode = null;
            refreshAssignmentButtons();
        }).bounds(gridLeft + 102, actionTop, 98, 20).build());

        stableAssignButton = addRenderableWidget(Button.builder(
                Component.translatable("gui.pickclimber.rules.stable"),
                button -> chooseAssignmentMode(SurfaceClassification.STABLE)
        ).bounds(gridLeft, actionTop + 24, 56, 20).build());
        unstableAssignButton = addRenderableWidget(Button.builder(
                Component.translatable("gui.pickclimber.rules.unstable"),
                button -> chooseAssignmentMode(SurfaceClassification.UNSTABLE)
        ).bounds(gridLeft + 60, actionTop + 24, 60, 20).build());
        unclimbableAssignButton = addRenderableWidget(Button.builder(
                Component.translatable("gui.pickclimber.rules.unclimbable"),
                button -> chooseAssignmentMode(SurfaceClassification.UNCLIMBABLE)
        ).bounds(gridLeft + 124, actionTop + 24, 76, 20).build());
        refreshAssignmentButtons();

        addRenderableWidget(Button.builder(Component.translatable("gui.pickclimber.rules.restore_selected"), button ->
                restoreSelected()
        ).bounds(gridLeft, actionTop + 48, GRID_WIDTH, 20).build());
    }

    private void addRulesControls() {
        wearSlider = addRenderableWidget(new WearSlider(
                sideLeft,
                sideTop + 12,
                140,
                pickaxeWear,
                value -> pickaxeWear = value
        ));
        addRenderableWidget(Button.builder(Component.translatable("gui.pickclimber.rules.reset_15"), button -> {
            pickaxeWear = ClimbingRulesProfile.DEFAULT_PICKAXE_WEAR;
            wearSlider.setWear(pickaxeWear);
        }).bounds(sideLeft + 172, sideTop + 12, 68, 20).build());

        miningButton = addRenderableWidget(Button.builder(miningMessage(), button -> {
            playerMiningEnabled = !playerMiningEnabled;
            button.setMessage(miningMessage());
        }).bounds(sideLeft, sideTop + 40, SIDE_WIDTH, 20).build());

        terminalsButton = addRenderableWidget(Button.builder(terminalsMessage(), button -> {
            unmineableTerminals = !unmineableTerminals;
            button.setMessage(terminalsMessage());
        }).bounds(sideLeft, sideTop + 64, SIDE_WIDTH, 20).build());

        activationButton = addRenderableWidget(Button.builder(activationMessage(), button -> {
            activationMode = activationMode == RuleBookActivationMode.PERMANENT
                    ? RuleBookActivationMode.TEMPORARY
                    : RuleBookActivationMode.PERMANENT;
            if (activationMode == RuleBookActivationMode.PERMANENT) {
                scope = RuleBookScope.WORLD;
                durationSeconds = 0;
            } else if (durationSeconds <= 0) {
                durationSeconds = 60;
            }
            rebuildWidgets();
        }).bounds(sideLeft, sideTop + 88, SIDE_WIDTH, 20).build());

        scopeButton = addRenderableWidget(Button.builder(scopeMessage(), button -> {
            scope = scope == RuleBookScope.WORLD ? RuleBookScope.PLAYER : RuleBookScope.WORLD;
            button.setMessage(scopeMessage());
        }).bounds(sideLeft, sideTop + 112, SIDE_WIDTH, 20).build());
        scopeButton.visible = activationMode == RuleBookActivationMode.TEMPORARY;

        durationField = new EditBox(
                font,
                sideLeft,
                sideTop + 152,
                SIDE_WIDTH,
                20,
                Component.translatable("gui.pickclimber.rules.duration_seconds")
        );
        durationField.setMaxLength(7);
        durationField.setFilter(value -> value.matches("\\d{0,7}"));
        durationField.setValue(Integer.toString(durationSeconds));
        durationField.setResponder(this::updateDurationValue);
        durationField.visible = activationMode == RuleBookActivationMode.TEMPORARY;
        addRenderableWidget(durationField);

        int saveTop = sideTop + 180;
        addRenderableWidget(Button.builder(Component.translatable("gui.pickclimber.rules.save_book"), button -> save())
                .bounds(sideLeft, saveTop, SIDE_WIDTH, 20)
                .build());
        addRenderableWidget(Button.builder(
                Component.translatable("gui.pickclimber.rules.exit_without_saving"),
                button -> onClose()
        ).bounds(sideLeft, saveTop + 24, SIDE_WIDTH, 20)
                .build());
    }

    private Button tabButton(ViewTab target, int x, int y, int buttonWidth, String key) {
        Button button = Button.builder(Component.translatable(key), clicked -> {
            tab = target;
            selected.clear();
            refreshTabButtons();
            rebuildFilter();
        }).bounds(x, y, buttonWidth, 20).build();
        button.active = target != tab;
        return button;
    }

    private void refreshTabButtons() {
        if (allTabButton != null) {
            allTabButton.active = tab != ViewTab.ALL;
        }
        if (stableTabButton != null) {
            stableTabButton.active = tab != ViewTab.STABLE;
        }
        if (unstableTabButton != null) {
            unstableTabButton.active = tab != ViewTab.UNSTABLE;
        }
        if (unclimbableTabButton != null) {
            unclimbableTabButton.active = tab != ViewTab.UNCLIMBABLE;
        }
    }

    private void chooseAssignmentMode(SurfaceClassification mode) {
        assignmentMode = mode;
        if (!selected.isEmpty()) {
            assignSelected(mode);
            selected.clear();
            rebuildFilter();
        }
        refreshAssignmentButtons();
    }

    private void refreshAssignmentButtons() {
        if (stableAssignButton != null) {
            stableAssignButton.active = assignmentMode != SurfaceClassification.STABLE;
        }
        if (unstableAssignButton != null) {
            unstableAssignButton.active = assignmentMode != SurfaceClassification.UNSTABLE;
        }
        if (unclimbableAssignButton != null) {
            unclimbableAssignButton.active = assignmentMode != SurfaceClassification.UNCLIMBABLE;
        }
    }

    private void rebuildFilter() {
        rebuildFilter(true);
    }

    private void rebuildFilter(boolean resetScroll) {
        String query = searchQuery.trim().toLowerCase(Locale.ROOT);
        filtered = catalog.stream()
                .filter(entry -> query.isEmpty() || entry.matches(query))
                .filter(entry -> tab == ViewTab.ALL || tab.matches(classification(entry.id())))
                .toList();
        if (resetScroll) {
            scrollRow = 0;
        }
        clampScroll();
    }

    private void renderGrid(GuiGraphics gui, int mouseX, int mouseY) {
        for (int cell = 0; cell < COLUMNS * visibleRows; cell++) {
            int index = scrollRow * COLUMNS + cell;
            int x = gridLeft + cell % COLUMNS * CELL;
            int y = gridTop + cell / COLUMNS * CELL;
            gui.fill(x + 1, y + 1, x + 19, y + 19, 0xCC202329);
            if (index >= filtered.size()) {
                continue;
            }
            BlockCatalogService.Entry entry = filtered.get(index);
            ResourceLocation id = entry.id();
            SurfaceClassification classification = classification(id);
            if (selected.contains(id)) {
                drawSelectionBorder(gui, x, y, 0xFFFFFFFF);
            } else if (classification != null) {
                drawSelectionBorder(gui, x, y, classificationColor(classification));
            }
            gui.renderItem(entry.stack(), x + 2, y + 2);
            if (mouseX >= x && mouseX < x + 20 && mouseY >= y && mouseY < y + 20) {
                gui.renderTooltip(font, entry.stack(), mouseX, mouseY);
            }
        }
    }

    private void renderScrollbar(GuiGraphics gui) {
        if (maxScrollRows() <= 0) {
            return;
        }
        int x = gridLeft + GRID_WIDTH + 3;
        int trackHeight = gridHeight();
        int thumbHeight = scrollbarThumbHeight(trackHeight);
        int travel = trackHeight - thumbHeight;
        int thumbTop = gridTop + (int) Math.round((double) scrollRow / maxScrollRows() * travel);
        gui.fill(x, gridTop, x + SCROLLBAR_WIDTH, gridTop + trackHeight, 0xAA15171B);
        gui.fill(x, thumbTop, x + SCROLLBAR_WIDTH, thumbTop + thumbHeight, 0xFF8A9099);
    }

    private void renderPanelLabels(GuiGraphics gui) {
        gui.drawString(
                font,
                Component.translatable("gui.pickclimber.rules.pickaxe_wear_label"),
                sideLeft,
                sideTop,
                0xAEB4BD,
                false
        );
        gui.drawString(
                font,
                Component.literal(Integer.toString(pickaxeWear)),
                sideLeft + 148,
                sideTop + 18,
                0xFFFFFF,
                false
        );
        if (activationMode == RuleBookActivationMode.TEMPORARY) {
            gui.drawString(
                    font,
                    Component.translatable("gui.pickclimber.rules.duration_seconds"),
                    sideLeft,
                    sideTop + 140,
                    0xAEB4BD,
                    false
            );
        }
    }

    private int classificationColor(SurfaceClassification classification) {
        return switch (classification) {
            case STABLE -> 0xFF55FF55;
            case UNSTABLE -> 0xFF55FFFF;
            case UNCLIMBABLE -> 0xFFFF5555;
        };
    }

    private void renderTabIndicator(GuiGraphics gui) {
        Button selectedTab = switch (tab) {
            case ALL -> allTabButton;
            case STABLE -> stableTabButton;
            case UNSTABLE -> unstableTabButton;
            case UNCLIMBABLE -> unclimbableTabButton;
        };
        if (selectedTab == null || !selectedTab.visible) {
            return;
        }
        int y = selectedTab.getY() + selectedTab.getHeight() - 2;
        gui.fill(selectedTab.getX() + 2, y, selectedTab.getX() + selectedTab.getWidth() - 2, y + 2, 0xFFAA55FF);
    }

    private void renderAssignmentButtonBorders(GuiGraphics gui) {
        drawButtonBorder(gui, stableAssignButton, 0xFF55FF55);
        drawButtonBorder(gui, unstableAssignButton, 0xFF55FFFF);
        drawButtonBorder(gui, unclimbableAssignButton, 0xFFFF5555);
    }

    private void drawButtonBorder(GuiGraphics gui, Button button, int color) {
        if (button == null || !button.visible) {
            return;
        }
        int x = button.getX();
        int y = button.getY();
        int right = x + button.getWidth();
        int bottom = y + button.getHeight();
        gui.fill(x - 1, y - 1, right + 1, y, color);
        gui.fill(x - 1, bottom, right + 1, bottom + 1, color);
        gui.fill(x - 1, y, x, bottom, color);
        gui.fill(right, y, right + 1, bottom, color);
    }

    private void renderNarrowScrollbar(GuiGraphics gui) {
        if (wideLayout || maxNarrowScrollOffset() <= 0) {
            return;
        }
        int x = width - 7;
        int top = NARROW_VIEW_TOP + 4;
        int bottom = height - NARROW_BOTTOM_MARGIN - 4;
        int trackHeight = Math.max(1, bottom - top);
        int viewport = Math.max(1, height - NARROW_VIEW_TOP - NARROW_BOTTOM_MARGIN);
        int thumbHeight = Math.max(18, trackHeight * viewport / Math.max(viewport, narrowContentHeight));
        int travel = Math.max(1, trackHeight - thumbHeight);
        int thumbTop = top + (int) Math.round((double) narrowScrollOffset / maxNarrowScrollOffset() * travel);
        gui.fill(x, top, x + 4, bottom, 0xAA15171B);
        gui.fill(x, thumbTop, x + 4, thumbTop + thumbHeight, 0xFF8A9099);
    }

    private boolean clickNarrowScrollbar(double mouseX, double mouseY) {
        if (wideLayout || maxNarrowScrollOffset() <= 0) {
            return false;
        }
        int x = width - 7;
        if (mouseX < x || mouseX >= x + 4
                || mouseY < NARROW_VIEW_TOP || mouseY >= height - NARROW_BOTTOM_MARGIN) {
            return false;
        }
        narrowScrollbarDragging = true;
        updateNarrowScrollFromMouse(mouseY);
        return true;
    }

    private void updateNarrowScrollFromMouse(double mouseY) {
        int top = NARROW_VIEW_TOP + 4;
        int bottom = height - NARROW_BOTTOM_MARGIN - 4;
        int trackHeight = Math.max(1, bottom - top);
        double normalized = (mouseY - top) / trackHeight;
        normalized = Math.max(0.0D, Math.min(1.0D, normalized));
        int updated = (int) Math.round(normalized * maxNarrowScrollOffset());
        if (updated != narrowScrollOffset) {
            narrowScrollOffset = updated;
            rebuildWidgets();
        }
    }

    private void drawSelectionBorder(GuiGraphics gui, int x, int y, int color) {
        gui.fill(x, y, x + 20, y + 1, color);
        gui.fill(x, y + 19, x + 20, y + 20, color);
        gui.fill(x, y + 1, x + 1, y + 19, color);
        gui.fill(x + 19, y + 1, x + 20, y + 19, color);
    }

    private boolean clickGrid(double mouseX, double mouseY) {
        if (mouseX < gridLeft || mouseX >= gridLeft + GRID_WIDTH
                || mouseY < gridTop || mouseY >= gridTop + gridHeight()) {
            return false;
        }
        int column = (int) (mouseX - gridLeft) / CELL;
        int row = (int) (mouseY - gridTop) / CELL;
        int index = (scrollRow + row) * COLUMNS + column;
        if (index >= filtered.size()) {
            return false;
        }
        ResourceLocation id = filtered.get(index).id();
        if (tab != ViewTab.ALL) {
            clearClassification(id);
            rebuildFilter(false);
            return true;
        }
        if (assignmentMode != null) {
            toggleClassification(id, assignmentMode);
            return true;
        }
        if (!selected.remove(id)) {
            selected.add(id);
        }
        return true;
    }

    private boolean clickScrollbar(double mouseX, double mouseY) {
        if (maxScrollRows() <= 0) {
            return false;
        }
        int x = gridLeft + GRID_WIDTH + 3;
        if (mouseX < x || mouseX >= x + SCROLLBAR_WIDTH
                || mouseY < gridTop || mouseY >= gridTop + gridHeight()) {
            return false;
        }
        scrollbarDragging = true;
        updateScrollFromMouse(mouseY);
        return true;
    }

    private void updateScrollFromMouse(double mouseY) {
        int trackHeight = gridHeight();
        int thumbHeight = scrollbarThumbHeight(trackHeight);
        int travel = Math.max(1, trackHeight - thumbHeight);
        double normalized = (mouseY - gridTop - thumbHeight / 2.0D) / travel;
        normalized = Math.max(0.0D, Math.min(1.0D, normalized));
        scrollRow = (int) Math.round(normalized * maxScrollRows());
    }

    private int scrollbarThumbHeight(int trackHeight) {
        int totalRows = Math.max(visibleRows, (filtered.size() + COLUMNS - 1) / COLUMNS);
        return Math.max(14, trackHeight * visibleRows / totalRows);
    }

    private void renderStatus(GuiGraphics gui) {
        Component selectedCount = Component.translatable(
                "gui.pickclimber.rules.selected_count",
                selected.size()
        );
        Component missingCount = Component.translatable(
                "gui.pickclimber.rules.missing_count",
                missingIds.size()
        );
        if (!wideLayout) {
            return;
        }

        gui.drawString(font, selectedCount, sideLeft, 24, 0xB7BDC6, false);
        gui.drawString(
                font,
                Component.translatable("gui.pickclimber.rules.cover", coverColor.getName()),
                sideLeft,
                36,
                0xB7BDC6,
                false
        );
        gui.drawString(font, missingCount, sideLeft, 48, missingColor(), false);
    }

    private int missingColor() {
        return missingIds.isEmpty() ? 0x8FD39F : 0xE3B66C;
    }

    private void selectVisiblePage() {
        int first = scrollRow * COLUMNS;
        int end = Math.min(filtered.size(), first + COLUMNS * visibleRows);
        for (int index = first; index < end; index++) {
            ResourceLocation id = filtered.get(index).id();
            if (assignmentMode == null || tab != ViewTab.ALL) {
                selected.add(id);
            } else {
                setClassification(id, assignmentMode);
            }
        }
    }

    private void assignSelected(SurfaceClassification classification) {
        for (ResourceLocation id : selected) {
            setClassification(id, classification);
        }
    }

    private void toggleClassification(ResourceLocation id, SurfaceClassification target) {
        if (classification(id) == target) {
            clearClassification(id);
        } else {
            setClassification(id, target);
        }
    }

    private void setClassification(ResourceLocation id, SurfaceClassification target) {
        removeClassification(id);
        clearedToUnclimbable.remove(id);
        switch (target) {
            case STABLE -> stable.add(id);
            case UNSTABLE -> unstable.add(id);
            case UNCLIMBABLE -> unclimbable.add(id);
        }
    }

    private void removeClassification(ResourceLocation id) {
        stable.remove(id);
        unstable.remove(id);
        unclimbable.remove(id);
    }

    private void clearClassification(ResourceLocation id) {
        removeClassification(id);
        clearedToUnclimbable.add(id);
    }

    private void restoreSelected() {
        selected.forEach(this::clearClassification);
        selected.clear();
        rebuildFilter();
    }

    private SurfaceClassification classification(ResourceLocation id) {
        if (unclimbable.contains(id)) {
            return SurfaceClassification.UNCLIMBABLE;
        }
        if (unstable.contains(id)) {
            return SurfaceClassification.UNSTABLE;
        }
        return stable.contains(id) ? SurfaceClassification.STABLE : null;
    }

    private Component miningMessage() {
        return Component.translatable(
                playerMiningEnabled
                        ? "gui.pickclimber.rules.player_mining_enabled"
                        : "gui.pickclimber.rules.player_mining_disabled"
        );
    }

    private Component terminalsMessage() {
        return Component.translatable(
                unmineableTerminals
                        ? "gui.pickclimber.rules.terminals_unmineable"
                        : "gui.pickclimber.rules.terminals_mineable"
        );
    }

    private Component activationMessage() {
        return Component.translatable(
                activationMode == RuleBookActivationMode.PERMANENT
                        ? "gui.pickclimber.rules.activation_permanent"
                        : "gui.pickclimber.rules.activation_temporary"
        );
    }

    private Component scopeMessage() {
        return Component.translatable(
                scope == RuleBookScope.WORLD
                        ? "gui.pickclimber.rules.scope_world"
                        : "gui.pickclimber.rules.scope_player"
        );
    }

    private void updateDurationValue(String value) {
        if (value.isEmpty()) {
            return;
        }
        try {
            durationSeconds = Math.max(0, Integer.parseInt(value));
        } catch (NumberFormatException ignored) {
        }
    }

    private void save() {
        if (durationField != null) {
            updateDurationValue(durationField.getValue());
        }

        // In the ALL tab, a white border is the editor's "left without a category" state.
        // Selected entries must therefore not keep their previous Stable/Unstable value in the payload.
        java.util.Set<ResourceLocation> completedStable = new java.util.HashSet<>(stable);
        java.util.Set<ResourceLocation> completedUnstable = new java.util.HashSet<>(unstable);
        completedStable.removeAll(selected);
        completedUnstable.removeAll(selected);

        java.util.Set<ResourceLocation> completedUnclimbable = new java.util.HashSet<>(unclimbable);
        completedUnclimbable.addAll(clearedToUnclimbable);
        completedUnclimbable.addAll(selected);
        for (BlockCatalogService.Entry entry : catalog) {
            ResourceLocation id = entry.id();
            if (!completedStable.contains(id) && !completedUnstable.contains(id)) {
                completedUnclimbable.add(id);
            }
        }
        ClimbingRulesProfile profile = new ClimbingRulesProfile(
                ClimbingRulesProfile.CURRENT_FORMAT_VERSION,
                draftProfileName,
                completedStable,
                completedUnstable,
                completedUnclimbable,
                UnlistedPolicy.UNCLIMBABLE,
                pickaxeWear,
                playerMiningEnabled,
                unmineableTerminals
        );
        ClimbingRuleBookDefinition definition = new ClimbingRuleBookDefinition(
                ClimbingRuleBookDefinition.CURRENT_FORMAT_VERSION,
                draftProfileName,
                coverColor,
                profile,
                activationMode,
                scope,
                durationSeconds
        );
        ClimbingRuleBookCodec.encodeToNbt(definition).result().ifPresent(tag ->
                ClimbingRulesClientRequests.updateRuleBook(tablePosition, sessionToken, tag)
        );
    }

    private int gridHeight() {
        return visibleRows * CELL;
    }

    private int maxScrollRows() {
        int totalRows = (filtered.size() + COLUMNS - 1) / COLUMNS;
        return Math.max(0, totalRows - visibleRows);
    }

    private void clampScroll() {
        scrollRow = Math.max(0, Math.min(maxScrollRows(), scrollRow));
    }

    private int maxNarrowScrollOffset() {
        return Math.max(0, narrowContentHeight - Math.max(NARROW_VIEW_TOP + 1, height - NARROW_BOTTOM_MARGIN));
    }

    private static Set<ResourceLocation> findMissingIds(ClimbingRulesProfile profile) {
        Set<ResourceLocation> ids = new LinkedHashSet<>();
        ids.addAll(profile.stableBlocks());
        ids.addAll(profile.unstableBlocks());
        ids.addAll(profile.unclimbableBlocks());
        ids.removeIf(id -> BuiltInRegistries.BLOCK.getOptional(id).isPresent());
        return Set.copyOf(ids);
    }

    private enum ViewTab {
        ALL,
        STABLE,
        UNSTABLE,
        UNCLIMBABLE;

        private boolean matches(SurfaceClassification classification) {
            return switch (this) {
                case ALL -> true;
                case STABLE -> classification == SurfaceClassification.STABLE;
                case UNSTABLE -> classification == SurfaceClassification.UNSTABLE;
                case UNCLIMBABLE -> classification == SurfaceClassification.UNCLIMBABLE;
            };
        }
    }

    private static final class WearSlider extends AbstractSliderButton {
        private final IntConsumer onChanged;

        private WearSlider(int x, int y, int width, int initialWear, IntConsumer onChanged) {
            super(x, y, width, 20, Component.empty(), clamp(initialWear) / 100.0D);
            this.onChanged = onChanged;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.empty());
        }

        @Override
        protected void applyValue() {
            onChanged.accept((int) Math.round(value * 100.0D));
        }

        private void setWear(int wear) {
            value = clamp(wear) / 100.0D;
            applyValue();
            updateMessage();
        }

        private static int clamp(int value) {
            return Math.max(0, Math.min(100, value));
        }
    }
}
