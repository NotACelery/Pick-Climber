package dev.maicra.pickclimber.climb;

import java.util.UUID;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

final class ToolLocator {
    private ToolLocator() {
    }

    static ItemStack findEquipped(Player player, UUID toolId) {
        if (ToolIdentity.matches(player.getMainHandItem(), toolId)) {
            return player.getMainHandItem();
        }
        if (ToolIdentity.matches(player.getOffhandItem(), toolId)) {
            return player.getOffhandItem();
        }
        return ItemStack.EMPTY;
    }

    static ItemStack findOwned(Player player, UUID toolId) {
        for (ItemStack stack : player.getInventory().items) {
            if (ClimbingToolClassifier.isClimbingTool(stack) && ToolIdentity.matches(stack, toolId)) {
                return stack;
            }
        }

        ItemStack offhand = player.getOffhandItem();
        if (ClimbingToolClassifier.isClimbingTool(offhand) && ToolIdentity.matches(offhand, toolId)) {
            return offhand;
        }
        return ItemStack.EMPTY;
    }
}
