package dev.maicra.pickclimber.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

final class StringIndicatorIconRenderer implements IndicatorIconRenderer {
    static final StringIndicatorIconRenderer INSTANCE = new StringIndicatorIconRenderer();
    private static final ItemStack RANGE_ICON = new ItemStack(Items.STRING);

    private StringIndicatorIconRenderer() {
    }

    @Override
    public void render(GuiGraphics gui, int x, int y, int color, float scale, float opacity) {
        gui.flush();
        RenderSystem.setShaderColor(
                colorChannel(color, 16),
                colorChannel(color, 8),
                colorChannel(color, 0),
                opacity
        );

        gui.pose().pushPose();
        gui.pose().translate(x, y, 0.0F);
        gui.pose().scale(scale, scale, 1.0F);
        try {
            gui.renderItem(RANGE_ICON, 0, 0);
            gui.flush();
        } finally {
            gui.pose().popPose();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    private static float colorChannel(int color, int shift) {
        return ((color >> shift) & 0xFF) / 255.0F;
    }
}
