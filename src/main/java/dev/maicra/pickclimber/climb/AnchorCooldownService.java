package dev.maicra.pickclimber.climb;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

final class AnchorCooldownService {
    private AnchorCooldownService() {
    }

    static boolean isCoolingDown(ItemStack stack, long gameTime) {
        return ToolIdentity.isCoolingDown(stack, gameTime);
    }

    static float fraction(ItemStack stack, long gameTime) {
        return ToolIdentity.cooldownFraction(stack, gameTime);
    }

    static int remainingTicks(ItemStack stack, long gameTime) {
        long until = ToolIdentity.cooldownUntil(stack);
        if (until <= gameTime) {
            return 0;
        }
        return (int) Math.min(Integer.MAX_VALUE, until - gameTime);
    }

    static void start(ServerPlayer player, ItemStack stack) {
        start(player, stack, ClimbTuning.ANCHOR_COOLDOWN_TICKS);
    }

    static void start(ServerPlayer player, ItemStack stack, int cooldownTicks) {
        startLocal(stack, player.level().getGameTime(), cooldownTicks);
        player.getInventory().setChanged();
    }

    static void clear(ServerPlayer player, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        clearLocal(stack);
        player.getInventory().setChanged();
    }

    static void startLocal(ItemStack stack, long gameTime, int cooldownTicks) {
        ToolIdentity.startCooldown(
                stack,
                gameTime + cooldownTicks,
                cooldownTicks
        );
    }

    static void startLocalRemaining(ItemStack stack, long gameTime, int cooldownTicks) {
        ToolIdentity.startCooldown(stack, gameTime + cooldownTicks);
    }

    static void clearLocal(ItemStack stack) {
        ToolIdentity.clearCooldown(stack);
    }

    static void startOtherEquippedTools(ServerPlayer player, InteractionHand activeHand) {
        long until = player.level().getGameTime() + ClimbTuning.ANCHOR_COOLDOWN_TICKS;
        for (InteractionHand hand : InteractionHand.values()) {
            if (hand == activeHand) {
                continue;
            }
            ItemStack equipped = player.getItemInHand(hand);
            if (ClimbingToolClassifier.isClimbingTool(equipped)) {
                ToolIdentity.startCooldown(
                        equipped,
                        until,
                        ClimbTuning.ANCHOR_COOLDOWN_TICKS
                );
            }
        }
        player.getInventory().setChanged();
    }
}
