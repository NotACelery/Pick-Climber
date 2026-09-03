package dev.maicra.pickclimber.client.rules;

import dev.maicra.pickclimber.ModItems;
import dev.maicra.pickclimber.rules.ClimbingRulesClientState;
import dev.maicra.pickclimber.rules.item.ClimbingRuleBookData;
import dev.maicra.pickclimber.rules.menu.ClimbingRulesTableMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.nio.file.Path;

public final class ClimbingRulesTableScreen extends AbstractContainerScreen<ClimbingRulesTableMenu> {
    private static final int PANEL_WIDTH = 232;
    private static final int PANEL_HEIGHT = 226;

    private EditBox profileName;
    private Button createButton;
    private Button importButton;
    private Button importCurrentButton;
    private Button editButton;
    private Button duplicateButton;
    private Button exportButton;
    private Button ejectButton;
    private Button restoreButton;
    private boolean exportOverwriteConfirmation;
    private String draftProfileName;

    public ClimbingRulesTableScreen(
            ClimbingRulesTableMenu menu,
            Inventory inventory,
            Component title
    ) {
        super(menu, inventory, title);
        imageWidth = PANEL_WIDTH;
        imageHeight = PANEL_HEIGHT;
        inventoryLabelY = 133;
    }

    @Override
    protected void init() {
        super.init();
        int left = leftPos + 50;
        int buttonWidth = 174;
        if (draftProfileName == null) {
            draftProfileName = Component.translatable("gui.pickclimber.rules.new_profile").getString();
        }
        profileName = new EditBox(font, left, topPos + 34, buttonWidth, 18, Component.empty());
        profileName.setMaxLength(64);
        profileName.setValue(draftProfileName);
        profileName.setResponder(value -> draftProfileName = value);
        addRenderableWidget(profileName);

        createButton = addRenderableWidget(Button.builder(
                Component.translatable("gui.pickclimber.rules.create_book"),
                button -> ClimbingRulesClientRequests.createRuleBook(menu.blockPos(), draftProfileName)
        ).bounds(left, topPos + 56, 85, 20).build());

        importButton = addRenderableWidget(Button.builder(
                Component.translatable("gui.pickclimber.rules.import_json"),
                button -> minecraft.setScreen(new ClimbingRulesImportScreen(this, menu.blockPos()))
        ).bounds(left + 89, topPos + 56, 85, 20).build());

        importCurrentButton = addRenderableWidget(Button.builder(
                Component.translatable("gui.pickclimber.rules.import_current"),
                button -> ClimbingRulesClientRequests.importCurrentRules(menu.blockPos())
        ).bounds(left, topPos + 80, buttonWidth, 20).build());

        editButton = addRenderableWidget(Button.builder(
                Component.translatable("gui.pickclimber.rules.edit_book"),
                button -> ClimbingRulesClientRequests.openEditor(menu.blockPos())
        ).bounds(left, topPos + 34, 85, 20).build());

        duplicateButton = addRenderableWidget(Button.builder(
                Component.translatable("gui.pickclimber.rules.duplicate_book"),
                button -> minecraft.setScreen(new ClimbingRuleBookDuplicateScreen(this))
        ).bounds(left + 89, topPos + 34, 85, 20).build());

        exportButton = addRenderableWidget(Button.builder(
                Component.translatable("gui.pickclimber.rules.export_json"),
                button -> handleExport()
        ).bounds(left, topPos + 58, 85, 20).build());

        ejectButton = addRenderableWidget(Button.builder(
                Component.translatable("gui.pickclimber.rules.eject"),
                button -> ClimbingRulesClientRequests.eject(menu.blockPos())
        ).bounds(left + 89, topPos + 58, 85, 20).build());

        restoreButton = addRenderableWidget(Button.builder(
                Component.translatable("gui.pickclimber.rules.restore_defaults"),
                button -> openRestoreConfirmation()
        ).bounds(left, topPos + 82, buttonWidth, 20).build());
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
        drawTableSlots(gui);
        drawDyePlaceholder(gui);
        drawPlayerInventorySlots(gui);
    }

    @Override
    protected void renderLabels(GuiGraphics gui, int mouseX, int mouseY) {
        gui.drawString(font, title, 8, 8, 0xFFFFFF, false);
        gui.drawString(
                font,
                Component.translatable("gui.pickclimber.rules.insert_hint"),
                8,
                20,
                0xAEB4BD,
                false
        );
    }

    private void updateControls() {
        var ruleBookStack = menu.ruleBook();
        boolean hasRuleBook = ruleBookStack.is(ModItems.CLIMBING_RULE_BOOK.get());
        boolean hasMaterialBook = menu.materialBook().is(Items.BOOK);

        profileName.visible = !hasRuleBook;
        createButton.visible = !hasRuleBook;
        createButton.active = hasMaterialBook;
        importButton.visible = !hasRuleBook;
        importButton.active = hasMaterialBook;
        importCurrentButton.visible = !hasRuleBook;
        importCurrentButton.active = hasMaterialBook && ClimbingRulesClientState.worldActiveDefinition().isPresent();
        editButton.visible = hasRuleBook;
        duplicateButton.visible = hasRuleBook;
        exportButton.visible = hasRuleBook;
        ejectButton.visible = hasRuleBook;

        restoreButton.active = ClimbingRulesClientState.worldRuntimeView().active();
    }

    private void handleExport() {
        var definition = ClimbingRuleBookData.readDefinitionValidated(menu.insertedItem());
        if (definition.isEmpty()) {
            return;
        }
        ClimbingRulesJsonFiles.FileResult<Path> exported = ClimbingRulesJsonFiles.exportRuleBook(
                definition.get(),
                exportOverwriteConfirmation
        );
        if (!exported.success() && "message.pickclimber.rules.json_exists".equals(exported.errorKey())) {
            exportOverwriteConfirmation = true;
            exportButton.setMessage(Component.translatable("gui.pickclimber.rules.confirm_overwrite"));
            return;
        }
        exportOverwriteConfirmation = false;
        exportButton.setMessage(Component.translatable("gui.pickclimber.rules.export_json"));
        showMessage(exported.success() ? "message.pickclimber.rules.json_exported" : exported.errorKey());
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
        gui.drawString(font, state, leftPos + 8, topPos + 130, rules.active() ? 0x7FE6A3 : 0xB8BDC5, false);
    }

    private void drawTableSlots(GuiGraphics gui) {
        drawInventorySlot(gui, leftPos + 24, topPos + 36);
        drawInventorySlot(gui, leftPos + 24, topPos + 58);
        drawInventorySlot(gui, leftPos + 24, topPos + 80);
    }

    private void drawDyePlaceholder(GuiGraphics gui) {
        if (!menu.dye().isEmpty()) {
            return;
        }
        gui.setColor(1.0F, 1.0F, 1.0F, 0.28F);
        gui.renderItem(new ItemStack(Items.WHITE_DYE), leftPos + 24, topPos + 80);
        gui.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void drawPlayerInventorySlots(GuiGraphics gui) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                drawInventorySlot(gui, leftPos + 35 + column * 18, topPos + 145 + row * 18);
            }
        }
        for (int column = 0; column < 9; column++) {
            drawInventorySlot(gui, leftPos + 35 + column * 18, topPos + 203);
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
