package dev.maicra.pickclimber.client;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.maicra.pickclimber.climb.AnchorIndicatorStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

final class AnchorIndicatorRenderer {
    private static final ItemStack RANGE_ICON = new ItemStack(Items.STRING);
    private AnchorIndicatorRenderer() {
    }

    static void render(Minecraft minecraft, Player player, GuiGraphics gui) {
        if (!(minecraft.hitResult instanceof BlockHitResult hit)
                || hit.getType() != HitResult.Type.BLOCK) {
            return;
        }

        AnchorIndicatorStatus status = AnchorIndicatorPolicy.statusFor(player, hit);
        if (status == AnchorIndicatorStatus.NONE) {
            return;
        }

        int iconX = gui.guiWidth() / 2 - ClientClimbDefaults.ANCHOR_ICON_SIZE / 2;
        int iconY = gui.guiHeight() / 2 + ClientClimbDefaults.ANCHOR_ICON_Y_OFFSET;
        int color = status.color();

        renderTintedRangeIcon(gui, iconX, iconY, color);
        renderRangeBorder(gui, iconX, iconY, ClientClimbDefaults.OPAQUE_ALPHA | color);
    }

    private static void renderTintedRangeIcon(GuiGraphics gui, int x, int y, int color) {
        gui.flush();
        RenderSystem.setShaderColor(
                colorChannel(color, 16),
                colorChannel(color, 8),
                colorChannel(color, 0),
                1.0F
        );

        try {
            gui.renderItem(RANGE_ICON, x, y);
            gui.flush();
        } finally {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    private static void renderRangeBorder(GuiGraphics gui, int x, int y, int color) {
        int right = x + ClientClimbDefaults.ANCHOR_ICON_SIZE;
        int bottom = y + ClientClimbDefaults.ANCHOR_ICON_SIZE;

        gui.fill(x - 1, y - 1, right + 1, y, color);
        gui.fill(x - 1, bottom, right + 1, bottom + 1, color);
        gui.fill(x - 1, y, x, bottom, color);
        gui.fill(right, y, right + 1, bottom, color);
    }

    private static float colorChannel(int color, int shift) {
        return ((color >> shift) & 0xFF) / 255.0F;
    }
}
