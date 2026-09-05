package dev.maicra.pickclimber.client;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

final class StringIndicatorIconRenderer implements IndicatorIconRenderer {
    static final StringIndicatorIconRenderer INSTANCE = new StringIndicatorIconRenderer();
    private static final ResourceLocation STRING_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "minecraft",
            "textures/item/string.png"
    );

    private StringIndicatorIconRenderer() {
    }

    @Override
    public void render(GuiGraphics gui, int x, int y, int color, float scale, float opacity) {
        gui.flush();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
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
            gui.blit(STRING_TEXTURE, 0, 0, 0.0F, 0.0F, 16, 16, 16, 16);
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
