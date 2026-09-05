package dev.maicra.pickclimber.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.IItemDecorator;

import dev.maicra.pickclimber.climb.ClimbManager;

public final class PickClimberItemDecorator implements IItemDecorator {
    public static final PickClimberItemDecorator INSTANCE = new PickClimberItemDecorator();

    private PickClimberItemDecorator() {
    }

    @Override
    public boolean render(
            GuiGraphics guiGraphics,
            Font font,
            ItemStack stack,
            int xOffset,
            int yOffset
    ) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !ClimbManager.isClimbingTool(stack)) {
            return false;
        }

        float fraction = ClimbManager.visualCooldownFraction(player, stack);
        if (fraction <= 0.0F) {
            return false;
        }

        int top = yOffset + Mth.floor(16.0F * (1.0F - fraction));
        int bottom = top + Mth.ceil(16.0F * fraction);

        guiGraphics.fill(
                RenderType.guiOverlay(),
                xOffset,
                top,
                xOffset + 16,
                bottom,
                Integer.MAX_VALUE
        );
        return false;
    }
}
