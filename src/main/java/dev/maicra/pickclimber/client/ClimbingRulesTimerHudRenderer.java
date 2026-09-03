package dev.maicra.pickclimber.client;

import dev.maicra.pickclimber.rules.ClimbingRulesClientState;
import dev.maicra.pickclimber.rules.TemporaryRuleBookClientState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

final class ClimbingRulesTimerHudRenderer {
    private static final int HOTBAR_OFFSET_Y = 46;

    private ClimbingRulesTimerHudRenderer() {
    }

    static void render(Minecraft minecraft, GuiGraphics gui) {
        if (minecraft.level == null) {
            return;
        }

        long gameTime = minecraft.level.getGameTime();
        long rulesRemaining = remaining(ClimbingRulesClientState.expiresAtGameTime(), gameTime);
        long bookRemaining = remaining(TemporaryRuleBookClientState.expiresAtGameTime(), gameTime);
        if (rulesRemaining <= 0L && bookRemaining <= 0L) {
            return;
        }

        if (rulesRemaining > 0L && bookRemaining > 0L) {
            drawCentered(
                    minecraft,
                    gui,
                    Component.translatable(
                            "gui.pickclimber.rules.timer_rules",
                            formatRemaining(rulesRemaining)
                    ),
                    HOTBAR_OFFSET_Y + 10
            );
            drawCentered(
                    minecraft,
                    gui,
                    Component.translatable(
                            "gui.pickclimber.rules.timer_book",
                            formatRemaining(bookRemaining)
                    ),
                    HOTBAR_OFFSET_Y
            );
            return;
        }

        long remaining = rulesRemaining > 0L ? rulesRemaining : bookRemaining;
        drawCentered(minecraft, gui, Component.literal(formatRemaining(remaining)), HOTBAR_OFFSET_Y);
    }

    private static long remaining(long expiresAtGameTime, long gameTime) {
        return expiresAtGameTime > gameTime ? expiresAtGameTime - gameTime : 0L;
    }

    private static void drawCentered(Minecraft minecraft, GuiGraphics gui, Component text, int bottomOffset) {
        int x = (gui.guiWidth() - minecraft.font.width(text)) / 2;
        int y = gui.guiHeight() - bottomOffset;
        gui.drawString(minecraft.font, text, x + 1, y + 1, 0x80000000, false);
        gui.drawString(minecraft.font, text, x, y, 0xFFFFFFFF, false);
    }

    static String formatRemaining(long ticks) {
        long seconds = Math.max(1L, (ticks + 19L) / 20L);
        long hours = seconds / 3600L;
        long minutes = (seconds % 3600L) / 60L;
        long remainingSeconds = seconds % 60L;
        return hours > 0L
                ? "%d:%02d:%02d".formatted(hours, minutes, remainingSeconds)
                : "%d:%02d".formatted(minutes, remainingSeconds);
    }
}
