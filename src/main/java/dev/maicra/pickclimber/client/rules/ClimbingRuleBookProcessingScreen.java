package dev.maicra.pickclimber.client.rules;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import dev.maicra.pickclimber.ModItems;
import dev.maicra.pickclimber.rules.menu.ClimbingRuleBookProcessingMenu;

public final class ClimbingRuleBookProcessingScreen
        extends AbstractContainerScreen<ClimbingRuleBookProcessingMenu> {
    private static final int PANEL_WIDTH = 220;
    private static final int PANEL_HEIGHT = 194;
    private static final DyeColor[] DYE_COLORS = DyeColor.values();

    public ClimbingRuleBookProcessingScreen(
            ClimbingRuleBookProcessingMenu menu,
            Inventory inventory,
            Component title
    ) {
        super(menu, inventory, title);
        imageWidth = PANEL_WIDTH;
        imageHeight = PANEL_HEIGHT;
        inventoryLabelY = 98;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(
                Component.translatable("gui.pickclimber.rules.back_to_table"),
                button -> ClimbingRulesClientRequests.openRulesTable(menu.blockPos())
        ).bounds(leftPos + imageWidth - 58, topPos + 6, 50, 20).build());
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        renderBackground(gui, mouseX, mouseY, partialTick);
        super.render(gui, mouseX, mouseY, partialTick);
        renderTooltip(gui, mouseX, mouseY);
        renderEmptySlotHints(gui, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics gui, float partialTick, int mouseX, int mouseY) {
        gui.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xF0202227);
        gui.fill(leftPos, topPos, leftPos + imageWidth, topPos + 1, 0xFFB0B4BC);
        gui.fill(leftPos, topPos + imageHeight - 1, leftPos + imageWidth, topPos + imageHeight, 0xFF555A62);
        drawInventorySlot(gui, leftPos + 46, topPos + 42);
        drawInventorySlot(gui, leftPos + 86, topPos + 42);
        drawInventorySlot(gui, leftPos + 142, topPos + 42);
        gui.drawString(font, Component.literal("+"), leftPos + 70, topPos + 46, 0xB8BDC5, false);
        gui.drawString(font, Component.literal("→"), leftPos + 114, topPos + 46, 0xB8BDC5, false);
        drawPlayerInventorySlots(gui);
    }

    @Override
    protected void renderLabels(GuiGraphics gui, int mouseX, int mouseY) {
        gui.drawString(font, title, 8, 8, 0xFFFFFF, false);
    }

    private void renderEmptySlotHints(GuiGraphics gui, int mouseX, int mouseY) {
        if (!menu.getSlot(0).hasItem() && overSlot(mouseX, mouseY, 46, 42)) {
            renderItemHint(gui, mouseX, mouseY, new ItemStack(ModItems.CLIMBING_RULE_BOOK.get()), ItemStack.EMPTY);
        }
        if (!menu.getSlot(1).hasItem() && overSlot(mouseX, mouseY, 86, 42)) {
            int frame = (int) ((System.currentTimeMillis() / 750L) % DYE_COLORS.length);
            ItemStack dye = new ItemStack(DyeItem.byColor(DYE_COLORS[frame]));
            renderItemHint(gui, mouseX, mouseY, new ItemStack(Items.BOOK), dye);
        }
    }

    private boolean overSlot(int mouseX, int mouseY, int slotX, int slotY) {
        return mouseX >= leftPos + slotX && mouseX < leftPos + slotX + 16
                && mouseY >= topPos + slotY && mouseY < topPos + slotY + 16;
    }

    private void renderItemHint(GuiGraphics gui, int mouseX, int mouseY, ItemStack left, ItemStack right) {
        int x = Math.min(width - 52, mouseX + 10);
        int y = Math.min(height - 24, mouseY + 8);
        gui.fill(x - 4, y - 4, x + 44, y + 20, 0xF0101010);
        gui.fill(x - 3, y - 3, x + 43, y - 2, 0xFF8A4DFF);
        gui.renderItem(left, x, y);
        if (!right.isEmpty()) {
            gui.drawString(font, Component.literal("/"), x + 18, y + 4, 0xB8BDC5, false);
            gui.renderItem(right, x + 26, y);
        }
    }

    private Component processingMode() {
        ItemStack source = menu.getSlot(0).getItem();
        ItemStack material = menu.getSlot(1).getItem();
        if (!source.is(ModItems.CLIMBING_RULE_BOOK.get())) {
            return Component.translatable("gui.pickclimber.rules.processing_waiting_source");
        }
        if (material.is(Items.BOOK)) {
            return Component.translatable("gui.pickclimber.rules.processing_clone");
        }
        if (material.getItem() instanceof DyeItem) {
            return Component.translatable("gui.pickclimber.rules.processing_dye");
        }
        return Component.translatable("gui.pickclimber.rules.processing_waiting_material");
    }

    private void drawInventorySlot(GuiGraphics gui, int x, int y) {
        gui.fill(x - 1, y - 1, x + 17, y + 17, 0xFF50545B);
        gui.fill(x, y, x + 16, y + 16, 0xFF171A1F);
    }

    private void drawPlayerInventorySlots(GuiGraphics gui) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                drawInventorySlot(gui, leftPos + 30 + column * 18, topPos + 110 + row * 18);
            }
        }
        for (int column = 0; column < 9; column++) {
            drawInventorySlot(gui, leftPos + 30 + column * 18, topPos + 168);
        }
    }
}
