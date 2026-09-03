package dev.maicra.pickclimber.client.rules;

import dev.maicra.pickclimber.rules.ClimbingRuleBookCodec;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class ClimbingRulesImportScreen extends Screen {
    private static final int ROW_HEIGHT = 24;
    private static final int MAX_VISIBLE_ROWS = 8;

    private final Screen parent;
    private final BlockPos tablePosition;
    private List<String> files = List.of();
    private int firstRow;
    private int visibleRows;

    public ClimbingRulesImportScreen(Screen parent, BlockPos tablePosition) {
        super(Component.translatable("gui.pickclimber.rules.import_title"));
        this.parent = parent;
        this.tablePosition = tablePosition;
    }

    @Override
    protected void init() {
        files = ClimbingRulesJsonFiles.listFiles();
        visibleRows = Math.max(1, Math.min(MAX_VISIBLE_ROWS, (height - 70) / ROW_HEIGHT));
        firstRow = Math.max(0, Math.min(firstRow, maxFirstRow()));
        rebuildFileButtons();
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        renderBackground(gui, mouseX, mouseY, partialTick);
        super.render(gui, mouseX, mouseY, partialTick);
        gui.drawCenteredString(font, title, width / 2, 14, 0xFFFFFF);
        if (files.isEmpty()) {
            gui.drawCenteredString(
                    font,
                    Component.translatable("gui.pickclimber.rules.import_empty"),
                    width / 2,
                    48,
                    0xAEB4BD
            );
        }
        gui.drawCenteredString(
                font,
                Component.translatable("gui.pickclimber.rules.import_directory"),
                width / 2,
                height - 44,
                0x8E949D
        );
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
        int buttonWidth = Math.min(300, width - 32);
        int left = (width - buttonWidth) / 2;
        for (int row = 0; row < visibleRows; row++) {
            int index = firstRow + row;
            if (index >= files.size()) {
                break;
            }
            String file = files.get(index);
            addRenderableWidget(Button.builder(Component.literal(file), button -> importFile(file))
                    .bounds(left, 36 + row * ROW_HEIGHT, buttonWidth, 20)
                    .build());
        }
        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> onClose())
                .bounds((width - 120) / 2, height - 24, 120, 20)
                .build());
    }

    private void importFile(String file) {
        ClimbingRulesJsonFiles.FileResult<dev.maicra.pickclimber.rules.ClimbingRuleBookDefinition> imported =
                ClimbingRulesJsonFiles.importRuleBook(file);
        if (!imported.success()) {
            showMessage(imported.errorKey());
            return;
        }
        ClimbingRuleBookCodec.encodeToNbt(imported.value()).result().ifPresentOrElse(
                tag -> ClimbingRulesClientRequests.importRuleBook(tablePosition, tag),
                () -> showMessage("message.pickclimber.rules.json_invalid_profile")
        );
    }

    private void showMessage(String key) {
        if (minecraft.player != null) {
            minecraft.player.displayClientMessage(Component.translatable(key), true);
        }
    }

    private int maxFirstRow() {
        return Math.max(0, files.size() - visibleRows);
    }
}
