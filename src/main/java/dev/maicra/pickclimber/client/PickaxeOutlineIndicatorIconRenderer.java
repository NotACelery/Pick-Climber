package dev.maicra.pickclimber.client;

import net.minecraft.client.gui.GuiGraphics;

final class PickaxeOutlineIndicatorIconRenderer implements IndicatorIconRenderer {
    static final PickaxeOutlineIndicatorIconRenderer INSTANCE = new PickaxeOutlineIndicatorIconRenderer();

    private PickaxeOutlineIndicatorIconRenderer() {
    }

    @Override
    public void render(GuiGraphics gui, int x, int y, int color, float scale, float opacity) {
        int argb = withOpacity(color, opacity);
        gui.pose().pushPose();
        gui.pose().translate(x, y, 0.0F);
        gui.pose().scale(scale, scale, 1.0F);
        try {
            renderHead(gui, argb);
            renderHandle(gui, argb);
        } finally {
            gui.pose().popPose();
        }
    }

    private static void renderHead(GuiGraphics gui, int color) {
        gui.fill(2, 2, 14, 3, color);
        gui.fill(1, 3, 4, 4, color);
        gui.fill(12, 3, 15, 4, color);
        gui.fill(4, 3, 12, 4, color);
    }

    private static void renderHandle(GuiGraphics gui, int color) {
        pixel(gui, 9, 4, color);
        pixel(gui, 8, 5, color);
        pixel(gui, 8, 6, color);
        pixel(gui, 7, 7, color);
        pixel(gui, 6, 8, color);
        pixel(gui, 6, 9, color);
        pixel(gui, 5, 10, color);
        pixel(gui, 4, 11, color);
        pixel(gui, 4, 12, color);
        pixel(gui, 3, 13, color);
    }

    private static void pixel(GuiGraphics gui, int x, int y, int color) {
        gui.fill(x, y, x + 2, y + 2, color);
    }

    private static int withOpacity(int color, float opacity) {
        int alpha = Math.round(Math.max(0.0F, Math.min(1.0F, opacity)) * 255.0F);
        return (alpha << 24) | (color & 0x00FFFFFF);
    }
}
