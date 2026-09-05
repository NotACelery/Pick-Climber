package dev.maicra.pickclimber.client.rules;

import java.nio.file.Path;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import dev.maicra.pickclimber.rules.ClimbingRuleBookDefinition;

public final class ClimbingRulesExportScreen extends Screen {
    private final Screen parent;
    private final ClimbingRuleBookDefinition definition;
    private EditBox fileNameField;

    public ClimbingRulesExportScreen(Screen parent, ClimbingRuleBookDefinition definition) {
        super(Component.translatable("gui.pickclimber.rules.export_title"));
        this.parent = parent;
        this.definition = definition;
    }

    @Override
    protected void init() {
        int panelWidth = Math.min(320, width - 24);
        int left = (width - panelWidth) / 2;
        int top = Math.max(36, height / 2 - 48);

        fileNameField = new EditBox(
                font,
                left,
                top + 24,
                panelWidth,
                20,
                Component.translatable("gui.pickclimber.rules.export_filename")
        );
        fileNameField.setMaxLength(64);
        fileNameField.setValue(ClimbingRulesJsonFiles.sanitizeFileName(definition.bookName()));
        addRenderableWidget(fileNameField);
        setInitialFocus(fileNameField);

        int half = (panelWidth - 4) / 2;
        addRenderableWidget(Button.builder(
                Component.translatable("gui.pickclimber.rules.export_confirm"),
                button -> export(false)
        ).bounds(left, top + 52, half, 20).build());
        addRenderableWidget(Button.builder(
                Component.translatable("gui.cancel"),
                button -> onClose()
        ).bounds(left + half + 4, top + 52, half, 20).build());
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        renderBackground(gui, mouseX, mouseY, partialTick);
        super.render(gui, mouseX, mouseY, partialTick);
        int top = Math.max(36, height / 2 - 48);
        gui.drawCenteredString(font, title, width / 2, top, 0xFFFFFF);
        gui.drawCenteredString(
                font,
                Component.translatable("gui.pickclimber.rules.export_internal_title", definition.bookName()),
                width / 2,
                top + 12,
                0xAEB4BD
        );
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    private void export(boolean overwrite) {
        String requestedName = fileNameField.getValue();
        ClimbingRulesJsonFiles.FileResult<Path> exported = ClimbingRulesJsonFiles.exportRuleBook(
                definition,
                requestedName,
                overwrite
        );
        if (exported.success()) {
            showMessage("message.pickclimber.rules.json_exported");
            minecraft.setScreen(parent);
            return;
        }
        if (!"message.pickclimber.rules.json_exists".equals(exported.errorKey())) {
            showMessage(exported.errorKey());
            return;
        }
        minecraft.setScreen(new ConfirmScreen(
                confirmed -> {
                    minecraft.setScreen(this);
                    if (confirmed) {
                        export(true);
                    }
                },
                Component.translatable("gui.pickclimber.rules.export_overwrite_title"),
                Component.translatable("gui.pickclimber.rules.export_overwrite_message"),
                Component.translatable("gui.pickclimber.rules.confirm_overwrite"),
                Component.translatable("gui.cancel")
        ));
    }

    private void showMessage(String key) {
        if (minecraft.player != null && key != null) {
            minecraft.player.displayClientMessage(Component.translatable(key), true);
        }
    }
}
