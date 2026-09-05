package dev.maicra.pickclimber.climb;

import java.util.UUID;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

final class ClimbSessionView {
    private ClimbSessionView() {
    }

    static InteractionHand activeHand(Player player) {
        if (player.level().isClientSide()) {
            ClientClimbState state = ClimbStateStore.client(player.getUUID());
            return state == null ? null : state.activeHand();
        }

        ServerClimbState state = ClimbStateStore.server(player.getUUID());
        return state == null ? null : state.activeHand();
    }

    static UUID activeToolId(Player player) {
        if (player.level().isClientSide()) {
            ClientClimbState state = ClimbStateStore.client(player.getUUID());
            return state == null ? null : state.toolId();
        }

        ServerClimbState state = ClimbStateStore.server(player.getUUID());
        return state == null ? null : state.toolId();
    }

    static boolean isActiveTool(Player player, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        UUID activeId = activeToolId(player);
        return activeId != null && ToolIdentity.matches(stack, activeId);
    }
}
