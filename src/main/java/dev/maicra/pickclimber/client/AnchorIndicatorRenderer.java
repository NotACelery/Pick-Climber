package dev.maicra.pickclimber.client;

import dev.maicra.pickclimber.climb.AnchorIndicatorStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

final class AnchorIndicatorRenderer {
    private AnchorIndicatorRenderer() {
    }

    static void render(Minecraft minecraft, Player player, GuiGraphics gui) {
        if (!(minecraft.hitResult instanceof BlockHitResult hit)
                || hit.getType() != HitResult.Type.BLOCK) {
            return;
        }

        ClientPickClimberBootstrap.ensureInstalled();
        AnchorIndicatorStatus status = AnchorIndicatorPolicy.statusFor(player, hit);
        if (status == AnchorIndicatorStatus.NONE) {
            return;
        }

        renderStatus(gui, status, PickClimberClientOptionsStore.current());
    }

    static void renderPreview(GuiGraphics gui, int centerX, int topY, AnchorIndicatorStatus status) {
        renderStatusAt(gui, centerX, topY, status, PickClimberClientOptionsStore.current());
    }

    private static void renderStatus(
            GuiGraphics gui,
            AnchorIndicatorStatus status,
            PickClimberClientOptions options
    ) {
        float scale = (float) options.iconScale();
        int displaySize = Math.max(1, Math.round(ClientClimbDefaults.ANCHOR_ICON_SIZE * scale));
        int centerX = gui.guiWidth() / 2;
        int topY = gui.guiHeight() / 2 + ClientClimbDefaults.ANCHOR_ICON_Y_OFFSET;
        renderStatusAt(gui, centerX, topY, status, options, displaySize);
    }

    private static void renderStatusAt(
            GuiGraphics gui,
            int centerX,
            int topY,
            AnchorIndicatorStatus status,
            PickClimberClientOptions options
    ) {
        float scale = (float) options.iconScale();
        int displaySize = Math.max(1, Math.round(ClientClimbDefaults.ANCHOR_ICON_SIZE * scale));
        renderStatusAt(gui, centerX, topY, status, options, displaySize);
    }

    private static void renderStatusAt(
            GuiGraphics gui,
            int centerX,
            int topY,
            AnchorIndicatorStatus status,
            PickClimberClientOptions options,
            int displaySize
    ) {
        int iconX = centerX - displaySize / 2;
        int color = status.color();

        if (options.iconOpacity() > 0.0D) {
            AnchorIndicatorIconRenderer.render(
                    gui,
                    options.indicatorStyle(),
                    iconX,
                    topY,
                    color,
                    (float) options.iconScale(),
                    (float) options.iconOpacity()
            );
        }
        if (options.showIndicatorBox() && options.boxOpacity() > 0.0D) {
            int boxColor = alpha(options.boxOpacity()) | color;
            renderRangeBorder(gui, iconX, topY, displaySize, boxColor);
        }
    }

    private static void renderRangeBorder(GuiGraphics gui, int x, int y, int size, int color) {
        int right = x + size;
        int bottom = y + size;

        gui.fill(x - 1, y - 1, right + 1, y, color);
        gui.fill(x - 1, bottom, right + 1, bottom + 1, color);
        gui.fill(x - 1, y, x, bottom, color);
        gui.fill(right, y, right + 1, bottom, color);
    }

    private static int alpha(double opacity) {
        int alpha = (int) Math.round(Math.max(0.0D, Math.min(1.0D, opacity)) * 255.0D);
        return alpha << 24;
    }
}
