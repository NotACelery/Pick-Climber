package dev.maicra.pickclimber.client;

import net.minecraft.client.gui.GuiGraphics;

interface IndicatorIconRenderer {
    void render(GuiGraphics gui, int x, int y, int color, float scale, float opacity);
}
