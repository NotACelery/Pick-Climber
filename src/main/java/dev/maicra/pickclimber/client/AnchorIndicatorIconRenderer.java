package dev.maicra.pickclimber.client;

import net.minecraft.client.gui.GuiGraphics;

final class AnchorIndicatorIconRenderer {
    private AnchorIndicatorIconRenderer() {
    }

    static void render(
            GuiGraphics gui,
            IndicatorStyle style,
            int x,
            int y,
            int color,
            float scale,
            float opacity
    ) {
        rendererFor(style).render(gui, x, y, color, scale, opacity);
    }

    private static IndicatorIconRenderer rendererFor(IndicatorStyle style) {
        return switch (style) {
            case STRING -> StringIndicatorIconRenderer.INSTANCE;
            case PICKAXE_OUTLINE -> PickaxeOutlineIndicatorIconRenderer.INSTANCE;
        };
    }
}
