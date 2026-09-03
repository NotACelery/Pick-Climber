package dev.maicra.pickclimber.client.rules;

import dev.maicra.pickclimber.rules.ClimbingRuleBookCodec;
import dev.maicra.pickclimber.rules.ClimbingRuleBookDefinition;
import dev.maicra.pickclimber.rules.ClimbingRulesProfile;
import dev.maicra.pickclimber.rules.RuleBookActivationMode;
import dev.maicra.pickclimber.rules.RuleBookScope;
import dev.maicra.pickclimber.rules.SurfaceClassification;
import dev.maicra.pickclimber.rules.UnlistedPolicy;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class ClimbingRulesEditorScreen extends Screen {
    private static final int CELL = 20;
    private static final int COLUMNS = 8;
    private static final int GRID_WIDTH = COLUMNS * CELL;
    private static final int GRID_TOP = 92;
    private static final int SIDE_WIDTH = 164;
    private static final int PANEL_GAP = 12;
    private static final int MIN_WIDE_WIDTH = 370;
    private static final int NARROW_SCROLL_STEP = 24;
    private static final int NARROW_VIEW_TOP = 20;
    private static final int NARROW_BOTTOM_MARGIN = 8;

    private final Screen parent;
    private final BlockPos tablePosition;
    private final int sessionToken;
    private final Set<ResourceLocation> stable;
    private final Set<ResourceLocation> unstable;
    private final Set<ResourceLocation> unclimbable;
    private final Set<ResourceLocation> selected = new LinkedHashSet<>();
    private final List<BlockCatalogService.Entry> catalog;
    private final Set<ResourceLocation> missingIds;

    private EditBox searchBox;
    private EditBox profileName;
    private EditBox durabilityField;
    private Button stableTabButton;
    private Button unstableTabButton;
    private Button unclimbableTabButton;
    private Button miningButton;
    private Button terminalsButton;
    private Button activationButton;
    private Button scopeButton;
    private EditBox durationField;
    private SurfaceClassification tab = SurfaceClassification.STABLE;
    private UnlistedPolicy unlistedPolicy;
    private int durabilityMultiplierPercent;
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
        unlistedPolicy = profile.unlistedPolicy();
        durabilityMultiplierPercent = profile.durabilityMultiplierPercent();
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
        profileName = new EditBox(font, gridLeft, 22 + contentOffset, GRID_WIDTH, 18, Component.empty());
        profileName.setMaxLength(ClimbingRulesProfile.MAX_PROFILE_NAME_LENGTH);
        profileName.setValue(draftProfileName);
        profileName.setResponder(value -> draftProfileName = value);
        addRenderableWidget(profileName);

        searchBox = new EditBox(font, gridLeft, 45 + contentOffset, GRID_WIDTH, 18, Component.empty());
        searchBox.setHint(Component.translatable("gui.pickclimber.rules.search"));
        searchBox.setValue(searchQuery);
        searchBox.setResponder(value -> {
            searchQuery = value;
            rebuildFilter();
        });
        addRenderableWidget(searchBox);

        int tabsTop = 67 + contentOffset;
        stableTabButton = addRenderableWidget(
                tabButton(SurfaceClassification.STABLE, gridLeft, tabsTop, "gui.pickclimber.rules.stable")
        );
        unstableTabButton = addRenderableWidget(tabButton(
                SurfaceClassification.UNSTABLE,
                gridLeft + 54,
                tabsTop,
                "gui.pickclimber.rules.unstable"
        ));
        unclimbableTabButton = addRenderableWidget(tabButton(
                SurfaceClassification.UNCLIMBABLE,
                gridLeft + 108,
                tabsTop,
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
        if (!wideLayout) {
            gui.disableScissor();
        }
        gui.drawCenteredString(font, title, width / 2, 8, 0xFFFFFF);
        renderStatus(gui);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && clickGrid(mouseX, mouseY)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        boolean overGrid = mouseX >= gridLeft
                && mouseX < gridLeft + GRID_WIDTH
                && mouseY >= gridTop
                && mouseY < gridTop + gridHeight();
        if (overGrid) {
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
        wideLayout = width >= MIN_WIDE_WIDTH && height >= 290;
        if (wideLayout) {
            narrowScrollOffset = 0;
            int totalWidth = GRID_WIDTH + PANEL_GAP + SIDE_WIDTH;
            gridLeft = Math.max(8, (width - totalWidth) / 2);
            gridTop = GRID_TOP;
            visibleRows = Math.max(1, Math.min(6, (height - GRID_TOP - 78) / CELL));
            sideLeft = gridLeft + GRID_WIDTH + PANEL_GAP;
            sideTop = 45;
            actionTop = gridTop + gridHeight() + 6;
            narrowContentHeight = 0;
            return;
        }

        gridLeft = Math.max(8, (width - GRID_WIDTH) / 2);
        visibleRows = 4;
        int baseActionTop = GRID_TOP + gridHeight() + 6;
        int baseSideTop = baseActionTop + 72;
        narrowContentHeight = baseSideTop + 216;
        narrowScrollOffset = Math.max(0, Math.min(maxNarrowScrollOffset(), narrowScrollOffset));
        gridTop = GRID_TOP - narrowScrollOffset;
        actionTop = baseActionTop - narrowScrollOffset;
        sideLeft = gridLeft;
        sideTop = baseSideTop - narrowScrollOffset;
    }

    private void addSelectionControls() {
        addRenderableWidget(Button.builder(Component.translatable("gui.pickclimber.rules.select_visible"), button ->
                selectVisiblePage()
        ).bounds(gridLeft, actionTop, 78, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.pickclimber.rules.clear_selection"), button ->
                selected.clear()
        ).bounds(gridLeft + 82, actionTop, 78, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.pickclimber.rules.set_stable"), button ->
                assignSelected(SurfaceClassification.STABLE)
        ).bounds(gridLeft, actionTop + 24, 50, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.pickclimber.rules.set_unstable"), button ->
                assignSelected(SurfaceClassification.UNSTABLE)
        ).bounds(gridLeft + 54, actionTop + 24, 50, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.pickclimber.rules.set_unclimbable"), button ->
                assignSelected(SurfaceClassification.UNCLIMBABLE)
        ).bounds(gridLeft + 108, actionTop + 24, 52, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.pickclimber.rules.restore_selected"), button ->
                restoreSelected()
        ).bounds(gridLeft, actionTop + 48, GRID_WIDTH, 20).build());
    }

    private void addRulesControls() {
        addRenderableWidget(Button.builder(unlistedMessage(), button -> {
            unlistedPolicy = unlistedPolicy == UnlistedPolicy.USE_PICK_CLIMBER_DEFAULTS
                    ? UnlistedPolicy.UNCLIMBABLE
                    : UnlistedPolicy.USE_PICK_CLIMBER_DEFAULTS;
            button.setMessage(unlistedMessage());
        }).bounds(sideLeft, sideTop, SIDE_WIDTH, 20).build());

        durabilityField = new EditBox(
                font,
                sideLeft,
                sideTop + 24,
                76,
                20,
                Component.translatable("gui.pickclimber.rules.durability")
        );
        durabilityField.setMaxLength(3);
        durabilityField.setFilter(value -> value.matches("\\d{0,3}"));
        durabilityField.setValue(Integer.toString(durabilityMultiplierPercent));
        durabilityField.setResponder(this::updateDurabilityValue);
        addRenderableWidget(durabilityField);

        addRenderableWidget(Button.builder(Component.translatable("gui.pickclimber.rules.reset_100"), button -> {
            durabilityMultiplierPercent = ClimbingRulesProfile.DEFAULT_DURABILITY_MULTIPLIER_PERCENT;
            durabilityField.setValue(Integer.toString(durabilityMultiplierPercent));
        }).bounds(sideLeft + 80, sideTop + 24, 84, 20).build());

        miningButton = addRenderableWidget(Button.builder(miningMessage(), button -> {
            playerMiningEnabled = !playerMiningEnabled;
            button.setMessage(miningMessage());
        }).bounds(sideLeft, sideTop + 48, SIDE_WIDTH, 20).build());

        terminalsButton = addRenderableWidget(Button.builder(terminalsMessage(), button -> {
            unmineableTerminals = !unmineableTerminals;
            button.setMessage(terminalsMessage());
        }).bounds(sideLeft, sideTop + 72, SIDE_WIDTH, 20).build());

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
        }).bounds(sideLeft, sideTop + 96, SIDE_WIDTH, 20).build());

        scopeButton = addRenderableWidget(Button.builder(scopeMessage(), button -> {
            scope = scope == RuleBookScope.WORLD ? RuleBookScope.PLAYER : RuleBookScope.WORLD;
            button.setMessage(scopeMessage());
        }).bounds(sideLeft, sideTop + 120, SIDE_WIDTH, 20).build());
        scopeButton.visible = activationMode == RuleBookActivationMode.TEMPORARY;

        durationField = new EditBox(
                font,
                sideLeft,
                sideTop + 144,
                SIDE_WIDTH,
                20,
                Component.translatable("gui.pickclimber.rules.duration")
        );
        durationField.setMaxLength(7);
        durationField.setFilter(value -> value.matches("\\d{0,7}"));
        durationField.setValue(Integer.toString(durationSeconds));
        durationField.setResponder(this::updateDurationValue);
        durationField.setHint(Component.translatable("gui.pickclimber.rules.duration_hint"));
        durationField.visible = activationMode == RuleBookActivationMode.TEMPORARY;
        addRenderableWidget(durationField);

        int saveTop = sideTop + 168;
        addRenderableWidget(Button.builder(Component.translatable("gui.pickclimber.rules.save_book"), button -> save())
                .bounds(sideLeft, saveTop, wideLayout ? SIDE_WIDTH : 78, 20)
                .build());
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(wideLayout ? sideLeft : sideLeft + 82, saveTop + 24, wideLayout ? SIDE_WIDTH : 78, 20)
                .build());
    }

    private Button tabButton(SurfaceClassification target, int x, int y, String key) {
        Button button = Button.builder(Component.translatable(key), clicked -> {
            tab = target;
            refreshTabButtons();
        }).bounds(x, y, 50, 20).build();
        button.active = target != tab;
        return button;
    }

    private void refreshTabButtons() {
        if (stableTabButton != null) {
            stableTabButton.active = tab != SurfaceClassification.STABLE;
        }
        if (unstableTabButton != null) {
            unstableTabButton.active = tab != SurfaceClassification.UNSTABLE;
        }
        if (unclimbableTabButton != null) {
            unclimbableTabButton.active = tab != SurfaceClassification.UNCLIMBABLE;
        }
    }

    private void rebuildFilter() {
        rebuildFilter(true);
    }

    private void rebuildFilter(boolean resetScroll) {
        String query = searchQuery.trim().toLowerCase(Locale.ROOT);
        if (query.isEmpty()) {
            filtered = catalog;
        } else {
            filtered = catalog.stream().filter(entry -> entry.matches(query)).toList();
        }
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
            gui.fill(x, y, x + 18, y + 18, 0xCC202329);
            if (index >= filtered.size()) {
                continue;
            }
            BlockCatalogService.Entry entry = filtered.get(index);
            ResourceLocation id = entry.id();
            if (selected.contains(id)) {
                drawSelectionBorder(gui, x, y, 0xFFFFFFFF);
            } else if (classification(id) == tab) {
                drawSelectionBorder(gui, x, y, 0xFF65C77A);
            }
            gui.renderItem(entry.stack(), x + 1, y + 1);
            if (mouseX >= x && mouseX < x + 18 && mouseY >= y && mouseY < y + 18) {
                gui.renderTooltip(font, entry.stack(), mouseX, mouseY);
            }
        }
    }

    private void drawSelectionBorder(GuiGraphics gui, int x, int y, int color) {
        gui.fill(x - 1, y - 1, x + 19, y, color);
        gui.fill(x - 1, y + 18, x + 19, y + 19, color);
        gui.fill(x - 1, y, x, y + 18, color);
        gui.fill(x + 18, y, x + 19, y + 18, color);
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
        if (!selected.remove(id)) {
            selected.add(id);
        }
        return true;
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
            gui.drawString(font, selectedCount, 8, 8, 0xB7BDC6, false);
            gui.drawString(
                    font,
                    missingCount,
                    width - font.width(missingCount) - 8,
                    8,
                    missingColor(),
                    false
            );
            return;
        }

        gui.drawString(font, selectedCount, sideLeft, 8, 0xB7BDC6, false);
        gui.drawString(
                font,
                Component.translatable("gui.pickclimber.rules.cover", coverColor.getName()),
                sideLeft,
                20,
                0xB7BDC6,
                false
        );
        gui.drawString(font, missingCount, sideLeft, 32, missingColor(), false);
    }

    private int missingColor() {
        return missingIds.isEmpty() ? 0x8FD39F : 0xE3B66C;
    }

    private void selectVisiblePage() {
        int first = scrollRow * COLUMNS;
        int end = Math.min(filtered.size(), first + COLUMNS * visibleRows);
        for (int index = first; index < end; index++) {
            selected.add(filtered.get(index).id());
        }
    }

    private void assignSelected(SurfaceClassification classification) {
        for (ResourceLocation id : selected) {
            stable.remove(id);
            unstable.remove(id);
            unclimbable.remove(id);
            switch (classification) {
                case STABLE -> stable.add(id);
                case UNSTABLE -> unstable.add(id);
                case UNCLIMBABLE -> unclimbable.add(id);
            }
        }
    }

    private void restoreSelected() {
        selected.forEach(id -> {
            stable.remove(id);
            unstable.remove(id);
            unclimbable.remove(id);
        });
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

    private Component unlistedMessage() {
        return Component.translatable(
                unlistedPolicy == UnlistedPolicy.UNCLIMBABLE
                        ? "gui.pickclimber.rules.unlisted_unclimbable"
                        : "gui.pickclimber.rules.unlisted_defaults"
        );
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

    private void updateDurabilityValue(String value) {
        if (value.isEmpty()) {
            return;
        }
        try {
            int parsed = Integer.parseInt(value);
            if (parsed >= 0 && parsed <= ClimbingRulesProfile.MAX_DURABILITY_MULTIPLIER_PERCENT) {
                durabilityMultiplierPercent = parsed;
            }
        } catch (NumberFormatException ignored) {
        }
    }

    private void save() {
        updateDurabilityValue(durabilityField.getValue());
        if (durationField != null) {
            updateDurationValue(durationField.getValue());
        }
        ClimbingRulesProfile profile = new ClimbingRulesProfile(
                ClimbingRulesProfile.CURRENT_FORMAT_VERSION,
                draftProfileName,
                stable,
                unstable,
                unclimbable,
                unlistedPolicy,
                durabilityMultiplierPercent,
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
}
