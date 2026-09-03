package dev.maicra.pickclimber.client.rules;

import dev.maicra.pickclimber.rules.ClimbingRuleBookDefinition;
import dev.maicra.pickclimber.rules.ClimbingRulesProfile;
import dev.maicra.pickclimber.rules.RuleBookActivationMode;
import dev.maicra.pickclimber.rules.RuleBookScope;
import dev.maicra.pickclimber.rules.UnlistedPolicy;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class ClimbingRuleBookViewerScreen extends Screen {
    private static final int CELL = 20;
    private static final int GRID_TOP = 76;
    private static final int MAX_COLUMNS = 10;

    private final Screen parent;
    private final ClimbingRuleBookDefinition definition;
    private final List<BlockCatalogService.Entry> catalog;
    private ViewerTab tab = ViewerTab.OVERVIEW;
    private EditBox searchBox;
    private String searchQuery = "";
    private List<BlockCatalogService.Entry> filtered = List.of();
    private int columns;
    private int visibleRows;
    private int scrollRow;
    private int gridLeft;

    public ClimbingRuleBookViewerScreen(Screen parent, ClimbingRuleBookDefinition definition) {
        super(Component.translatable("gui.pickclimber.rules.viewer_title"));
        this.parent = parent;
        this.definition = definition;
        this.catalog = BlockCatalogService.entries();
    }

    @Override
    protected void init() {
        columns = Math.max(4, Math.min(MAX_COLUMNS, (width - 32) / CELL));
        gridLeft = (width - columns * CELL) / 2;
        visibleRows = Math.max(1, (height - GRID_TOP - 38) / CELL);
        addTabButtons();
        if (tab != ViewerTab.OVERVIEW) {
            searchBox = new EditBox(
                    font,
                    Math.max(16, width / 2 - 120),
                    50,
                    Math.min(240, width - 32),
                    18,
                    Component.translatable("gui.pickclimber.rules.search")
            );
            searchBox.setMaxLength(96);
            searchBox.setValue(searchQuery);
            searchBox.setResponder(value -> {
                searchQuery = value;
                refreshFilter();
            });
            addRenderableWidget(searchBox);
            refreshFilter();
        }
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds((width - 120) / 2, height - 24, 120, 20)
                .build());
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        renderBackground(gui, mouseX, mouseY, partialTick);
        super.render(gui, mouseX, mouseY, partialTick);
        gui.drawCenteredString(font, Component.literal(definition.bookName()), width / 2, 8, 0xFFFFFF);
        if (tab == ViewerTab.OVERVIEW) {
            renderOverview(gui);
        } else {
            renderGrid(gui, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (tab == ViewerTab.OVERVIEW || scrollY == 0.0D) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        int previous = scrollRow;
        scrollRow = Math.max(0, Math.min(maxScrollRows(), scrollRow + (scrollY > 0.0D ? -1 : 1)));
        return previous != scrollRow || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (minecraft != null && minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    private void addTabButtons() {
        int buttonWidth = Math.max(58, Math.min(90, (width - 24) / ViewerTab.values().length - 4));
        int totalWidth = ViewerTab.values().length * buttonWidth + (ViewerTab.values().length - 1) * 4;
        int left = (width - totalWidth) / 2;
        int index = 0;
        for (ViewerTab candidate : ViewerTab.values()) {
            Button button = Button.builder(candidate.title(), clicked -> {
                tab = candidate;
                scrollRow = 0;
                rebuildWidgets();
            }).bounds(left + index * (buttonWidth + 4), 26, buttonWidth, 20).build();
            button.active = candidate != tab;
            addRenderableWidget(button);
            index++;
        }
    }

    private void refreshFilter() {
        if (tab == ViewerTab.OVERVIEW) {
            filtered = List.of();
            return;
        }
        Set<ResourceLocation> ids = idsFor(tab);
        String query = searchQuery.trim().toLowerCase(Locale.ROOT);
        filtered = catalog.stream()
                .filter(entry -> ids.contains(entry.id()))
                .filter(entry -> query.isEmpty() || entry.matches(query))
                .toList();
        scrollRow = Math.max(0, Math.min(scrollRow, maxScrollRows()));
    }

    private void renderOverview(GuiGraphics gui) {
        ClimbingRulesProfile profile = definition.profile();
        int left = Math.max(16, width / 2 - 150);
        int top = 60;
        int line = 16;
        int assigned = profile.stableBlocks().size()
                + profile.unstableBlocks().size()
                + profile.unclimbableBlocks().size();
        drawOverviewLine(gui, left, top, "gui.pickclimber.rules.viewer_cover",
                Component.translatable("color.minecraft." + definition.coverColor().getName()));
        drawOverviewLine(gui, left, top + line, "gui.pickclimber.rules.viewer_activation",
                Component.translatable(definition.activationMode() == RuleBookActivationMode.PERMANENT
                        ? "gui.pickclimber.rules.activation_permanent"
                        : "gui.pickclimber.rules.activation_temporary"));
        if (definition.activationMode() == RuleBookActivationMode.TEMPORARY) {
            drawOverviewLine(gui, left, top + line * 2, "gui.pickclimber.rules.viewer_scope",
                    Component.translatable(definition.scope() == RuleBookScope.WORLD
                            ? "gui.pickclimber.rules.scope_world"
                            : "gui.pickclimber.rules.scope_player"));
            drawOverviewLine(gui, left, top + line * 3, "gui.pickclimber.rules.viewer_duration",
                    Component.translatable("gui.pickclimber.rules.viewer_seconds", definition.durationSeconds()));
        }
        int offset = definition.activationMode() == RuleBookActivationMode.TEMPORARY ? 4 : 2;
        drawOverviewLine(gui, left, top + line * offset, "gui.pickclimber.rules.viewer_unlisted",
                Component.translatable(profile.unlistedPolicy() == UnlistedPolicy.UNCLIMBABLE
                        ? "gui.pickclimber.rules.unlisted_unclimbable"
                        : "gui.pickclimber.rules.unlisted_defaults"));
        drawOverviewLine(gui, left, top + line * (offset + 1), "gui.pickclimber.rules.viewer_durability",
                Component.literal(profile.durabilityMultiplierPercent() + "%"));
        drawOverviewLine(gui, left, top + line * (offset + 2), "gui.pickclimber.rules.viewer_player_mining",
                Component.translatable(profile.playerMiningEnabled()
                        ? "gui.pickclimber.rules.player_mining_enabled"
                        : "gui.pickclimber.rules.player_mining_disabled"));
        drawOverviewLine(gui, left, top + line * (offset + 3), "gui.pickclimber.rules.viewer_terminals",
                Component.translatable(profile.unmineableTerminals()
                        ? "gui.pickclimber.rules.terminals_unmineable"
                        : "gui.pickclimber.rules.terminals_mineable"));
        drawOverviewLine(gui, left, top + line * (offset + 4), "gui.pickclimber.rules.viewer_assigned",
                Component.literal(Integer.toString(assigned)));
    }

    private void drawOverviewLine(GuiGraphics gui, int x, int y, String labelKey, Component value) {
        Component label = Component.translatable(labelKey);
        gui.drawString(font, label, x, y, 0xAEB4BD, false);
        gui.drawString(font, value, x + 132, y, 0xFFFFFF, false);
    }

    private void renderGrid(GuiGraphics gui, int mouseX, int mouseY) {
        for (int cell = 0; cell < columns * visibleRows; cell++) {
            int index = scrollRow * columns + cell;
            int x = gridLeft + cell % columns * CELL;
            int y = GRID_TOP + cell / columns * CELL;
            gui.fill(x, y, x + 18, y + 18, 0xCC202329);
            if (index >= filtered.size()) {
                continue;
            }
            BlockCatalogService.Entry entry = filtered.get(index);
            gui.renderItem(entry.stack(), x + 1, y + 1);
            if (mouseX >= x && mouseX < x + 18 && mouseY >= y && mouseY < y + 18) {
                gui.renderTooltip(font, entry.stack(), mouseX, mouseY);
            }
        }
        gui.drawString(
                font,
                Component.translatable("gui.pickclimber.rules.viewer_visible", filtered.size()),
                8,
                height - 20,
                0x8E949D,
                false
        );
    }

    private Set<ResourceLocation> idsFor(ViewerTab selectedTab) {
        return switch (selectedTab) {
            case STABLE -> definition.profile().stableBlocks();
            case UNSTABLE -> definition.profile().unstableBlocks();
            case UNCLIMBABLE -> definition.profile().unclimbableBlocks();
            case OVERVIEW -> Set.of();
        };
    }

    private int maxScrollRows() {
        int totalRows = (filtered.size() + columns - 1) / columns;
        return Math.max(0, totalRows - visibleRows);
    }

    private enum ViewerTab {
        OVERVIEW("gui.pickclimber.rules.viewer_overview"),
        STABLE("gui.pickclimber.rules.stable"),
        UNSTABLE("gui.pickclimber.rules.unstable"),
        UNCLIMBABLE("gui.pickclimber.rules.unclimbable");

        private final String titleKey;

        ViewerTab(String titleKey) {
            this.titleKey = titleKey;
        }

        Component title() {
            return Component.translatable(titleKey);
        }
    }
}
