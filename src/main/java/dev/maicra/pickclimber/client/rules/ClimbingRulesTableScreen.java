package dev.maicra.pickclimber.client.rules;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Items;

import dev.maicra.pickclimber.ModItems;
import dev.maicra.pickclimber.rules.ClimbingRulesClientState;
import dev.maicra.pickclimber.rules.item.ClimbingRuleBookData;
import dev.maicra.pickclimber.rules.menu.ClimbingRulesTableMenu;

public final class ClimbingRulesTableScreen extends AbstractContainerScreen<ClimbingRulesTableMenu> {
    private static final int PANEL_WIDTH = 260;
    private static final int PANEL_HEIGHT = 240;
    private static final int BUTTON_WIDTH = 100;
    private static final int WIDE_BUTTON_WIDTH = 204;

    private Button createButton;
    private Button importButton;
    private Button editButton;
    private Button processingButton;
    private Button exportButton;
    private Button clearButton;
    private Button restoreButton;

    public ClimbingRulesTableScreen(
            ClimbingRulesTableMenu menu,
            Inventory inventory,
            Component title
    ) {
        super(menu, inventory, title);
        imageWidth = PANEL_WIDTH;
        imageHeight = PANEL_HEIGHT;
        inventoryLabelY = 147;
    }

    @Override
    protected void init() {
        super.init();
        int left = leftPos + 28;
        int right = left + BUTTON_WIDTH + 4;
        int rowOne = topPos + 70;
        int rowTwo = topPos + 94;

        createButton = addRenderableWidget(Button.builder(
                Component.translatable("gui.pickclimber.rules.create_book"),
                button -> ClimbingRulesClientRequests.createRuleBook(
                        menu.blockPos(),
                        Component.translatable("gui.pickclimber.rules.new_profile").getString()
                )
        ).bounds(left, rowOne, BUTTON_WIDTH, 20).build());

        importButton = addRenderableWidget(Button.builder(
                Component.translatable("gui.pickclimber.rules.import_json"),
                button -> minecraft.setScreen(new ClimbingRulesImportScreen(this, menu.blockPos()))
        ).bounds(right, rowOne, BUTTON_WIDTH, 20).build());

        editButton = addRenderableWidget(Button.builder(
                Component.translatable("gui.pickclimber.rules.edit_book"),
                button -> ClimbingRulesClientRequests.openEditor(menu.blockPos())
        ).bounds(left, rowOne, BUTTON_WIDTH, 20).build());

        processingButton = addRenderableWidget(Button.builder(
                Component.translatable("gui.pickclimber.rules.process_book"),
                button -> ClimbingRulesClientRequests.openProcessing(menu.blockPos())
        ).bounds(right, rowOne, BUTTON_WIDTH, 20).build());

        exportButton = addRenderableWidget(Button.builder(
                Component.translatable("gui.pickclimber.rules.export_json"),
                button -> handleExport()
        ).bounds(left, rowTwo, BUTTON_WIDTH, 20).build());

        clearButton = addRenderableWidget(Button.builder(
                Component.translatable("gui.pickclimber.rules.clear_book"),
                button -> ClimbingRulesClientRequests.clearRuleBook(menu.blockPos())
        ).bounds(right, rowTwo, BUTTON_WIDTH, 20).build());

        restoreButton = addRenderableWidget(Button.builder(
                Component.translatable("gui.pickclimber.rules.restore_defaults"),
                button -> openRestoreConfirmation()
        ).bounds(left, topPos + 118, WIDE_BUTTON_WIDTH, 20).build());
        updateControls();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        updateControls();
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        renderBackground(gui, mouseX, mouseY, partialTick);
        super.render(gui, mouseX, mouseY, partialTick);
        renderTooltip(gui, mouseX, mouseY);
        renderWorldRules(gui);
    }

    @Override
    protected void renderBg(GuiGraphics gui, float partialTick, int mouseX, int mouseY) {
        gui.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xF0202227);
        gui.fill(leftPos, topPos, leftPos + imageWidth, topPos + 1, 0xFFB0B4BC);
        gui.fill(leftPos, topPos + imageHeight - 1, leftPos + imageWidth, topPos + imageHeight, 0xFF555A62);
        drawInventorySlot(gui, leftPos + 122, topPos + 42);
        drawPlayerInventorySlots(gui);
    }

    @Override
    protected void renderLabels(GuiGraphics gui, int mouseX, int mouseY) {
        gui.drawString(font, title, 8, 8, 0xFFFFFF, false);
        gui.drawCenteredString(
                font,
                Component.translatable("gui.pickclimber.rules.work_slot_hint"),
                imageWidth / 2,
                22,
                0xAEB4BD
        );
    }

    private void updateControls() {
        var stack = menu.workItem();
        boolean vanillaBook = stack.is(Items.BOOK);
        boolean ruleBook = stack.is(ModItems.CLIMBING_RULE_BOOK.get());
        boolean validRuleBook = ClimbingRuleBookData.hasCurrentSchema(stack);

        createButton.visible = vanillaBook;
        importButton.visible = vanillaBook;
        editButton.visible = validRuleBook;
        processingButton.visible = validRuleBook;
        exportButton.visible = validRuleBook;
        clearButton.visible = ruleBook;

        restoreButton.active = ClimbingRulesClientState.worldRuntimeView().active();
    }

    private void handleExport() {
        ClimbingRulesClientRequests.exportRuleBook(menu.blockPos());
    }

    private void openRestoreConfirmation() {
        minecraft.setScreen(new ConfirmScreen(
                confirmed -> {
                    minecraft.setScreen(this);
                    if (confirmed) {
                        ClimbingRulesClientRequests.restoreWorldDefaults(menu.blockPos());
                    }
                },
                Component.translatable("gui.pickclimber.rules.restore_confirm_title"),
                Component.translatable("gui.pickclimber.rules.restore_confirm_message"),
                Component.translatable("gui.pickclimber.rules.restore_confirm_yes"),
                Component.translatable("gui.pickclimber.rules.restore_confirm_no")
        ));
    }

    private void renderWorldRules(GuiGraphics gui) {
        var rules = ClimbingRulesClientState.worldRuntimeView();
        Component state = rules.active()
                ? Component.translatable("gui.pickclimber.rules.world_profile", rules.profileName())
                : Component.translatable("gui.pickclimber.rules.world_defaults");
        gui.drawString(font, state, leftPos + 8, topPos + 144, rules.active() ? 0x7FE6A3 : 0xB8BDC5, false);
    }

    private void drawPlayerInventorySlots(GuiGraphics gui) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                drawInventorySlot(gui, leftPos + 49 + column * 18, topPos + 159 + row * 18);
            }
        }
        for (int column = 0; column < 9; column++) {
            drawInventorySlot(gui, leftPos + 49 + column * 18, topPos + 217);
        }
    }

    private void drawInventorySlot(GuiGraphics gui, int x, int y) {
        gui.fill(x - 1, y - 1, x + 17, y + 17, 0xFF50545B);
        gui.fill(x, y, x + 16, y + 16, 0xFF171A1F);
    }

    public ClimbingRulesTableMenu tableMenu() {
        return menu;
    }

    private void showMessage(String key) {
        if (minecraft.player != null && key != null) {
            minecraft.player.displayClientMessage(Component.translatable(key), true);
        }
    }
}
