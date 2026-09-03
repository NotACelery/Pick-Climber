package dev.maicra.pickclimber.client.rules;

import dev.maicra.pickclimber.rules.block.ClimbingRuleDispenserBlockEntity;
import dev.maicra.pickclimber.rules.menu.ClimbingRuleDispenserMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class ClimbingRuleDispenserScreen extends AbstractContainerScreen<ClimbingRuleDispenserMenu> {
    private static final int PANEL_WIDTH = 214;
    private static final int PANEL_HEIGHT = 174;

    public ClimbingRuleDispenserScreen(
            ClimbingRuleDispenserMenu menu,
            Inventory inventory,
            Component title
    ) {
        super(menu, inventory, title);
        imageWidth = PANEL_WIDTH;
        imageHeight = PANEL_HEIGHT;
        inventoryLabelY = 80;
    }

    @Override
    protected void init() {
        super.init();
        int buttonX = leftPos + 70;
        int buttonY = topPos + 34;
        addRenderableWidget(Button.builder(
                Component.literal("-10"),
                button -> changeLifetime(-10)
        ).bounds(buttonX, buttonY, 34, 20).build());
        addRenderableWidget(Button.builder(
                Component.literal("-1"),
                button -> changeLifetime(-1)
        ).bounds(buttonX + 38, buttonY, 30, 20).build());
        addRenderableWidget(Button.builder(
                Component.literal("+1"),
                button -> changeLifetime(1)
        ).bounds(buttonX + 72, buttonY, 30, 20).build());
        addRenderableWidget(Button.builder(
                Component.literal("+10"),
                button -> changeLifetime(10)
        ).bounds(buttonX + 106, buttonY, 34, 20).build());
    }

    @Override
    protected void renderBg(GuiGraphics gui, float partialTick, int mouseX, int mouseY) {
        gui.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xF0202227);
        gui.fill(leftPos, topPos, leftPos + imageWidth, topPos + 1, 0xFFB0B4BC);
        gui.fill(leftPos, topPos + imageHeight - 1, leftPos + imageWidth, topPos + imageHeight, 0xFF555A62);
        drawInventorySlot(gui, leftPos + 25, topPos + 35);
        drawPlayerInventorySlots(gui);
    }

    @Override
    protected void renderLabels(GuiGraphics gui, int mouseX, int mouseY) {
        gui.drawString(font, title, 8, 8, 0xFFFFFF, false);
        gui.drawString(
                font,
                Component.translatable("gui.pickclimber.rules.dispenser.master"),
                8,
                22,
                0xB8BDC5,
                false
        );
        gui.drawString(
                font,
                Component.translatable("gui.pickclimber.rules.dispenser.lifetime", menu.lifetimeSeconds()),
                70,
                58,
                0xFFFFFF,
                false
        );
        gui.drawString(
                font,
                Component.translatable("gui.pickclimber.rules.dispenser.issue_hint"),
                8,
                70,
                0xAEB4BD,
                false
        );
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        renderBackground(gui, mouseX, mouseY, partialTick);
        super.render(gui, mouseX, mouseY, partialTick);
        renderTooltip(gui, mouseX, mouseY);
    }

    private void changeLifetime(int delta) {
        int updated = ClimbingRuleDispenserBlockEntity.clampLifetime(menu.lifetimeSeconds() + delta);
        ClimbingRulesClientRequests.updateRuleDispenserLifetime(menu.blockPos(), updated);
    }

    private void drawInventorySlot(GuiGraphics gui, int x, int y) {
        gui.fill(x - 1, y - 1, x + 17, y + 17, 0xFF1B1D21);
        gui.fill(x, y, x + 16, y + 16, 0xFF555A62);
    }

    private void drawPlayerInventorySlots(GuiGraphics gui) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                drawInventorySlot(gui, leftPos + 26 + column * 18, topPos + 92 + row * 18);
            }
        }
        for (int column = 0; column < 9; column++) {
            drawInventorySlot(gui, leftPos + 26 + column * 18, topPos + 150);
        }
    }
}
