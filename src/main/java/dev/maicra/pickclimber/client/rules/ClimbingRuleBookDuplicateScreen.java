package dev.maicra.pickclimber.client.rules;

import dev.maicra.pickclimber.rules.ClimbingRuleBookDefinition;
import dev.maicra.pickclimber.rules.item.ClimbingRuleBookData;
import dev.maicra.pickclimber.rules.menu.ClimbingRulesTableMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Items;

import java.util.Optional;

public final class ClimbingRuleBookDuplicateScreen extends Screen {
    private static final int PANEL_WIDTH = 286;
    private static final int PANEL_HEIGHT = 184;
    private static final int MAX_COPIES = 64;

    private final ClimbingRulesTableScreen parent;
    private EditBox copiesField;
    private Button minusButton;
    private Button plusButton;
    private Button confirmButton;
    private int copies = 1;

    public ClimbingRuleBookDuplicateScreen(ClimbingRulesTableScreen parent) {
        super(Component.translatable("gui.pickclimber.rules.duplicate_title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int left = (width - PANEL_WIDTH) / 2;
        int top = (height - PANEL_HEIGHT) / 2;
        int fieldWidth = 56;
        int fieldLeft = left + (PANEL_WIDTH - fieldWidth) / 2;

        minusButton = addRenderableWidget(Button.builder(Component.literal("-"), button -> setCopies(copies - 1))
                .bounds(fieldLeft - 26, top + 98, 22, 20)
                .build());
        copiesField = new EditBox(font, fieldLeft, top + 99, fieldWidth, 18, Component.empty());
        copiesField.setMaxLength(2);
        copiesField.setFilter(value -> value.isEmpty() || value.chars().allMatch(Character::isDigit));
        copiesField.setValue(Integer.toString(copies));
        copiesField.setResponder(this::readCopies);
        addRenderableWidget(copiesField);
        plusButton = addRenderableWidget(Button.builder(Component.literal("+"), button -> setCopies(copies + 1))
                .bounds(fieldLeft + fieldWidth + 4, top + 98, 22, 20)
                .build());

        confirmButton = addRenderableWidget(Button.builder(
                Component.translatable("gui.pickclimber.rules.duplicate_confirm"),
                button -> confirm()
        ).bounds(left + 34, top + 132, 104, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> onClose())
                .bounds(left + 148, top + 132, 104, 20)
                .build());
        updateControls();
    }

    @Override
    public void tick() {
        super.tick();
        updateControls();
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        renderBackground(gui, mouseX, mouseY, partialTick);
        int left = (width - PANEL_WIDTH) / 2;
        int top = (height - PANEL_HEIGHT) / 2;
        gui.fill(left, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, 0xF0202227);
        gui.fill(left, top, left + PANEL_WIDTH, top + 1, 0xFFB0B4BC);
        gui.fill(left, top + PANEL_HEIGHT - 1, left + PANEL_WIDTH, top + PANEL_HEIGHT, 0xFF555A62);
        super.render(gui, mouseX, mouseY, partialTick);
        renderDetails(gui, left, top);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    private void renderDetails(GuiGraphics gui, int left, int top) {
        ClimbingRulesTableMenu menu = parent.tableMenu();
        Optional<ClimbingRuleBookDefinition> source = ClimbingRuleBookData.readDefinitionValidated(menu.ruleBook());
        Component sourceName = source
                .<Component>map(definition -> Component.literal(definition.bookName()))
                .orElseGet(() -> Component.translatable("gui.pickclimber.rules.duplicate_invalid_source"));
        gui.drawCenteredString(font, title, width / 2, top + 12, 0xFFFFFF);
        gui.drawString(font, Component.translatable("gui.pickclimber.rules.duplicate_source", sourceName),
                left + 18, top + 36, 0xD9DDE3, false);
        gui.drawString(font, Component.translatable(
                "gui.pickclimber.rules.duplicate_material",
                menu.materialBook().is(Items.BOOK) ? menu.materialBook().getCount() : 0
        ), left + 18, top + 52, 0xB7BDC6, false);

        Component dyeText = source.map(definition -> coverMessage(menu, definition))
                .orElseGet(() -> Component.literal("-"));
        gui.drawString(font, Component.translatable("gui.pickclimber.rules.duplicate_cover", dyeText),
                left + 18, top + 68, 0xB7BDC6, false);
        gui.drawCenteredString(font, Component.translatable("gui.pickclimber.rules.duplicate_count"),
                width / 2, top + 86, 0xD9DDE3);
        gui.drawCenteredString(font, Component.translatable("gui.pickclimber.rules.duplicate_output", copies),
                width / 2, top + 122, confirmButton.active ? 0x7FE6A3 : 0xC67A7A);
    }

    private Component coverMessage(ClimbingRulesTableMenu menu, ClimbingRuleBookDefinition source) {
        Optional<DyeColor> dye = dyeColor(menu);
        DyeColor target = dye.orElse(source.coverColor());
        boolean recolor = target != source.coverColor();
        String colorKey = "color.minecraft." + target.getName();
        if (!recolor) {
            return Component.translatable("gui.pickclimber.rules.duplicate_cover_inherited",
                    Component.translatable(colorKey));
        }
        return Component.translatable(
                "gui.pickclimber.rules.duplicate_cover_override",
                Component.translatable(colorKey),
                menu.dye().getCount()
        );
    }

    private void updateControls() {
        if (confirmButton == null) {
            return;
        }
        ClimbingRulesTableMenu menu = parent.tableMenu();
        Optional<ClimbingRuleBookDefinition> source = ClimbingRuleBookData.readDefinitionValidated(menu.ruleBook());
        int materialCount = menu.materialBook().is(Items.BOOK) ? menu.materialBook().getCount() : 0;
        boolean resourcesValid = source.isPresent() && copies >= 1 && copies <= Math.min(MAX_COPIES, materialCount);
        if (resourcesValid && source.isPresent()) {
            Optional<DyeColor> dye = dyeColor(menu);
            if (dye.isPresent() && dye.get() != source.get().coverColor()) {
                resourcesValid = menu.dye().getCount() >= copies;
            }
        }
        confirmButton.active = resourcesValid;
        minusButton.active = copies > 1;
        plusButton.active = copies < MAX_COPIES;
    }

    private Optional<DyeColor> dyeColor(ClimbingRulesTableMenu menu) {
        return menu.dye().getItem() instanceof DyeItem dyeItem
                ? Optional.of(dyeItem.getDyeColor())
                : Optional.empty();
    }

    private void readCopies(String value) {
        if (value.isEmpty()) {
            copies = 0;
            updateControls();
            return;
        }
        try {
            copies = Math.max(0, Math.min(MAX_COPIES, Integer.parseInt(value)));
        } catch (NumberFormatException ignored) {
            copies = 0;
        }
        updateControls();
    }

    private void setCopies(int value) {
        copies = Math.max(1, Math.min(MAX_COPIES, value));
        if (copiesField != null) {
            copiesField.setValue(Integer.toString(copies));
        }
        updateControls();
    }

    private void confirm() {
        if (!confirmButton.active) {
            return;
        }
        ClimbingRulesClientRequests.duplicateRuleBook(parent.tableMenu().blockPos(), copies);
        minecraft.setScreen(parent);
    }
}
