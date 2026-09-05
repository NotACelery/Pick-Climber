package dev.maicra.pickclimber.client;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import dev.maicra.pickclimber.rules.ClimbingRulesClientState;
import dev.maicra.pickclimber.rules.TemporaryRuleBookClientState;
import dev.maicra.pickclimber.rules.item.TemporaryRuleBookData;

final class ClimbingRulesTimerHudRenderer {
    private static final int EVENT_TIMER_OFFSET_Y = 56;

    private ClimbingRulesTimerHudRenderer() {
    }

    static void render(Minecraft minecraft, GuiGraphics gui) {
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }

        long gameTime = minecraft.level.getGameTime();
        List<BookTimer> bookTimers = bookTimers(minecraft, gameTime);
        long rulesRemaining = remaining(ClimbingRulesClientState.expiresAtGameTime(), gameTime);
        if (bookTimers.isEmpty() && rulesRemaining <= 0L) {
            long fallback = remaining(TemporaryRuleBookClientState.expiresAtGameTime(), gameTime);
            if (fallback > 0L) {
                drawCentered(minecraft, gui, Component.literal(formatRemaining(fallback)), EVENT_TIMER_OFFSET_Y);
            }
            return;
        }

        int line = 0;
        for (BookTimer timer : bookTimers) {
            String label = timer.name().isBlank() ? "Book" : timer.name();
            Component text = Component.literal(label + "  " + formatRemaining(timer.remainingTicks()));
            drawCentered(minecraft, gui, text, EVENT_TIMER_OFFSET_Y + line * 10);
            line++;
        }
        if (rulesRemaining > 0L) {
            drawCentered(
                    minecraft,
                    gui,
                    Component.translatable("gui.pickclimber.rules.timer_rules", formatRemaining(rulesRemaining)),
                    EVENT_TIMER_OFFSET_Y + line * 10
            );
        }
    }

    private static List<BookTimer> bookTimers(Minecraft minecraft, long gameTime) {
        List<BookTimer> timers = new ArrayList<>();
        for (int slot = 0; slot < minecraft.player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = minecraft.player.getInventory().getItem(slot);
            TemporaryRuleBookData.readValidated(stack).ifPresent(data -> {
                long remaining = remaining(data.expiresAtGameTime(), gameTime);
                if (data.owner().equals(minecraft.player.getUUID()) && remaining > 0L) {
                    timers.add(new BookTimer(data.bookName(), data.expiresAtGameTime(), remaining));
                }
            });
        }
        timers.sort(Comparator.comparingLong(BookTimer::expiresAtGameTime));
        return timers;
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

    private record BookTimer(String name, long expiresAtGameTime, long remainingTicks) {
    }
}
