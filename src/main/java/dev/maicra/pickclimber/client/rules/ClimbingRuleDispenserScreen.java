package dev.maicra.pickclimber.client.rules;

import dev.maicra.pickclimber.rules.block.ClimbingRuleDispenserBlockEntity;
import dev.maicra.pickclimber.rules.menu.ClimbingRuleDispenserMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class ClimbingRuleDispenserScreen extends AbstractContainerScreen<ClimbingRuleDispenserMenu> {
    private static final int PANEL_WIDTH = 236;
    private static final int PANEL_HEIGHT = 210;
    private LifetimeSlider lifetimeSlider;

    public ClimbingRuleDispenserScreen(
            ClimbingRuleDispenserMenu menu,
            Inventory inventory,
            Component title
    ) {
        super(menu, inventory, title);
        imageWidth = PANEL_WIDTH;
        imageHeight = PANEL_HEIGHT;
        inventoryLabelY = 110;
    }

    @Override
    protected void init() {
        super.init();
        lifetimeSlider = new LifetimeSlider(
                leftPos + 72,
                topPos + 44,
                104,
                menu.lifetimeSeconds()
        );
        addRenderableWidget(lifetimeSlider);
        addRenderableWidget(Button.builder(
                Component.translatable("gui.pickclimber.rules.dispenser.test_copy"),
                button -> ClimbingRulesClientRequests.dispenseRuleBookTest(menu.blockPos())
        ).bounds(leftPos + 72, topPos + 72, 140, 20).build());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && lifetimeSlider != null && lifetimeSlider.isMouseOver(mouseX, mouseY)) {
            lifetimeSlider.beginDrag(mouseX);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0 && lifetimeSlider != null && lifetimeSlider.dragging()) {
            lifetimeSlider.dragTo(mouseX);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && lifetimeSlider != null && lifetimeSlider.dragging()) {
            lifetimeSlider.endDrag();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    protected void renderBg(GuiGraphics gui, float partialTick, int mouseX, int mouseY) {
        gui.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xF0202227);
        gui.fill(leftPos, topPos, leftPos + imageWidth, topPos + 1, 0xFFB0B4BC);
        gui.fill(leftPos, topPos + imageHeight - 1, leftPos + imageWidth, topPos + imageHeight, 0xFF555A62);
        drawInventorySlot(gui, leftPos + 27, topPos + 47);
        drawPlayerInventorySlots(gui);
    }

    @Override
    protected void renderLabels(GuiGraphics gui, int mouseX, int mouseY) {
        gui.drawString(font, title, 8, 8, 0xFFFFFF, false);
        gui.drawString(
                font,
                Component.translatable("gui.pickclimber.rules.dispenser.source"),
                8,
                24,
                0xB8BDC5,
                false
        );
        gui.drawString(
                font,
                Component.translatable("gui.pickclimber.rules.dispenser.transport_lifetime"),
                72,
                26,
                0xAEB4BD,
                false
        );
        gui.drawString(
                font,
                Component.translatable(
                        "gui.pickclimber.rules.dispenser.seconds_short",
                        lifetimeSlider == null ? menu.lifetimeSeconds() : lifetimeSlider.seconds()),
                182,
                50,
                0xFFFFFF,
                false
        );
        gui.drawString(
                font,
                Component.translatable("gui.pickclimber.rules.dispenser.config_hint"),
                8,
                98,
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

    private final class LifetimeSlider extends AbstractSliderButton {
        private int seconds;
        private boolean dragging;

        private LifetimeSlider(int x, int y, int width, int initialSeconds) {
            super(
                    x,
                    y,
                    width,
                    20,
                    Component.empty(),
                    toSliderValue(initialSeconds)
            );
            seconds = ClimbingRuleDispenserBlockEntity.clampLifetime(initialSeconds);
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.empty());
        }

        @Override
        protected void applyValue() {
            int updated = fromSliderValue(value);
            if (updated == seconds) {
                return;
            }
            seconds = updated;
            ClimbingRulesClientRequests.updateRuleDispenserLifetime(menu.blockPos(), seconds);
        }

        private int seconds() {
            return seconds;
        }

        private void beginDrag(double mouseX) {
            dragging = true;
            dragTo(mouseX);
        }

        private void dragTo(double mouseX) {
            value = Math.max(0.0D, Math.min(1.0D, (mouseX - getX()) / (double) getWidth()));
            applyValue();
        }

        private void endDrag() {
            dragging = false;
        }

        private boolean dragging() {
            return dragging;
        }

        private static double toSliderValue(int seconds) {
            int clamped = ClimbingRuleDispenserBlockEntity.clampLifetime(seconds);
            int range = ClimbingRuleDispenserBlockEntity.MAX_LIFETIME_SECONDS
                    - ClimbingRuleDispenserBlockEntity.MIN_LIFETIME_SECONDS;
            return (clamped - ClimbingRuleDispenserBlockEntity.MIN_LIFETIME_SECONDS) / (double) range;
        }

        private static int fromSliderValue(double value) {
            int range = ClimbingRuleDispenserBlockEntity.MAX_LIFETIME_SECONDS
                    - ClimbingRuleDispenserBlockEntity.MIN_LIFETIME_SECONDS;
            int seconds = ClimbingRuleDispenserBlockEntity.MIN_LIFETIME_SECONDS
                    + (int) Math.round(value * range);
            return ClimbingRuleDispenserBlockEntity.clampLifetime(seconds);
        }
    }

    private void drawInventorySlot(GuiGraphics gui, int x, int y) {
        gui.fill(x - 1, y - 1, x + 17, y + 17, 0xFF1B1D21);
        gui.fill(x, y, x + 16, y + 16, 0xFF555A62);
    }

    private void drawPlayerInventorySlots(GuiGraphics gui) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                drawInventorySlot(gui, leftPos + 36 + column * 18, topPos + 122 + row * 18);
            }
        }
        for (int column = 0; column < 9; column++) {
            drawInventorySlot(gui, leftPos + 36 + column * 18, topPos + 180);
        }
    }
}
