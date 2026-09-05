package dev.maicra.pickclimber.client.rules;

import java.util.List;

import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import dev.maicra.pickclimber.rules.ClimbingRuleBookCodec;
import dev.maicra.pickclimber.rules.ClimbingRuleBookDefinition;
import dev.maicra.pickclimber.rules.ClimbingRulesProfile;
import dev.maicra.pickclimber.rules.DefaultRuleProfileFactory;

public final class ClimbingRulesImportScreen extends Screen {
    private static final int ROW_HEIGHT = 24;
    private static final int MAX_VISIBLE_ROWS = 7;
    private static final int DELETE_WIDTH = 24;

    private final Screen parent;
    private final BlockPos tablePosition;
    private List<ClimbingRulesJsonFiles.ImportEntry> files = List.of();
    private int firstRow;
    private int visibleRows;

    public ClimbingRulesImportScreen(Screen parent, BlockPos tablePosition) {
        super(Component.translatable("gui.pickclimber.rules.import_title"));
        this.parent = parent;
        this.tablePosition = tablePosition;
    }

    @Override
    protected void init() {
        files = ClimbingRulesJsonFiles.listImportEntries();
        visibleRows = Math.max(1, Math.min(MAX_VISIBLE_ROWS, (height - 122) / ROW_HEIGHT));
        firstRow = Math.max(0, Math.min(firstRow, maxFirstRow()));
        rebuildFileButtons();
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        renderBackground(gui, mouseX, mouseY, partialTick);
        super.render(gui, mouseX, mouseY, partialTick);
        gui.drawCenteredString(font, title, width / 2, 10, 0xFFFFFF);
        if (files.isEmpty()) {
            gui.drawCenteredString(
                    font,
                    Component.translatable("gui.pickclimber.rules.import_empty"),
                    width / 2,
                    Math.min(height - 64, 88),
                    0xAEB4BD
            );
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY == 0.0D || files.size() <= visibleRows) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        int previous = firstRow;
        firstRow = Math.max(0, Math.min(maxFirstRow(), firstRow + (scrollY > 0.0D ? -1 : 1)));
        if (previous != firstRow) {
            rebuildWidgets();
            return true;
        }
        return false;
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    Screen parentScreen() {
        return parent;
    }

    private void rebuildFileButtons() {
        int buttonWidth = Math.min(340, width - 24);
        int left = (width - buttonWidth) / 2;
        int top = 30;
        addRenderableWidget(Button.builder(
                Component.translatable("gui.pickclimber.rules.import_default_world"),
                button -> confirmDefaultImport()
        ).bounds(left, top, buttonWidth, 20).build());
        addRenderableWidget(Button.builder(
                Component.translatable("gui.pickclimber.rules.import_current_world"),
                button -> confirmCurrentImport()
        ).bounds(left, top + ROW_HEIGHT, buttonWidth, 20).build());

        int filesTop = top + ROW_HEIGHT * 2 + 6;
        int entryWidth = buttonWidth - DELETE_WIDTH - 4;
        for (int row = 0; row < visibleRows; row++) {
            int index = firstRow + row;
            if (index >= files.size()) {
                break;
            }
            ClimbingRulesJsonFiles.ImportEntry entry = files.get(index);
            int rowY = filesTop + row * ROW_HEIGHT;
            addRenderableWidget(Button.builder(
                    Component.literal(entry.title()),
                    button -> confirmFileImport(entry)
            ).bounds(left, rowY, entryWidth, 20).build());
            addRenderableWidget(Button.builder(
                    Component.literal("🗑"),
                    button -> confirmDelete(entry)
            ).bounds(left + entryWidth + 4, rowY, DELETE_WIDTH, 20).build());
        }

        int footerY = height - 24;
        addRenderableWidget(Button.builder(
                Component.translatable("gui.pickclimber.rules.open_rules_folder"),
                button -> openRulesFolder()
        ).bounds(left, footerY, (buttonWidth - 4) / 2, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> onClose())
                .bounds(left + (buttonWidth + 4) / 2, footerY, (buttonWidth - 4) / 2, 20)
                .build());
    }

    private void confirmDefaultImport() {
        String name = Component.translatable("gui.pickclimber.rules.import_default_world").getString();
        ClimbingRulesProfile profile = DefaultRuleProfileFactory.create(name);
        ClimbingRuleBookDefinition definition = ClimbingRuleBookDefinition.permanentWorld(name, profile);
        openImportConfirmation(name, () -> importDefinition(definition));
    }

    private void confirmCurrentImport() {
        String name = Component.translatable("gui.pickclimber.rules.import_current_world").getString();
        openImportConfirmation(name, () -> {
            ClimbingRulesClientRequests.importCurrentRules(tablePosition);
            minecraft.setScreen(parent);
        });
    }

    private void confirmFileImport(ClimbingRulesJsonFiles.ImportEntry entry) {
        openImportConfirmation(entry.title(), () -> importFile(entry.fileName()));
    }

    private void openImportConfirmation(String title, Runnable confirmedAction) {
        minecraft.setScreen(new ConfirmScreen(
                confirmed -> {
                    minecraft.setScreen(this);
                    if (confirmed) {
                        confirmedAction.run();
                    }
                },
                Component.translatable("gui.pickclimber.rules.import_confirm_title"),
                Component.translatable("gui.pickclimber.rules.import_confirm_message", title),
                Component.translatable("gui.pickclimber.rules.import_confirm_yes"),
                Component.translatable("gui.cancel")
        ));
    }

    private void confirmDelete(ClimbingRulesJsonFiles.ImportEntry entry) {
        minecraft.setScreen(new ConfirmScreen(
                confirmed -> {
                    minecraft.setScreen(this);
                    if (confirmed) {
                        deleteFile(entry.fileName());
                    }
                },
                Component.translatable("gui.pickclimber.rules.delete_confirm_title"),
                Component.translatable("gui.pickclimber.rules.delete_confirm_message", entry.title()),
                Component.translatable("gui.pickclimber.rules.delete_confirm_yes"),
                Component.translatable("gui.cancel")
        ));
    }

    private void deleteFile(String fileName) {
        ClimbingRulesJsonFiles.FileResult<Boolean> deleted = ClimbingRulesJsonFiles.deleteRuleBook(fileName);
        if (!deleted.success()) {
            showMessage(deleted.errorKey());
            return;
        }
        showMessage("message.pickclimber.rules.json_deleted");
        rebuildWidgets();
    }

    private void openRulesFolder() {
        if (!ClimbingRulesJsonFiles.ensureDirectory()) {
            showMessage("message.pickclimber.rules.json_export_failed");
            return;
        }
        Util.getPlatform().openPath(ClimbingRulesJsonFiles.directory());
    }

    private void importFile(String file) {
        ClimbingRulesJsonFiles.FileResult<ClimbingRuleBookDefinition> imported =
                ClimbingRulesJsonFiles.importRuleBook(file);
        if (!imported.success()) {
            showMessage(imported.errorKey());
            return;
        }
        importDefinition(imported.value());
    }

    private void importDefinition(ClimbingRuleBookDefinition definition) {
        ClimbingRuleBookCodec.encodeToNbt(definition).result().ifPresentOrElse(
                tag -> {
                    ClimbingRulesClientRequests.importRuleBook(tablePosition, tag);
                    minecraft.setScreen(parent);
                },
                () -> showMessage("message.pickclimber.rules.json_invalid_profile")
        );
    }

    private void showMessage(String key) {
        if (minecraft.player != null && key != null) {
            minecraft.player.displayClientMessage(Component.translatable(key), true);
        }
    }

    private int maxFirstRow() {
        return Math.max(0, files.size() - visibleRows);
    }
}
