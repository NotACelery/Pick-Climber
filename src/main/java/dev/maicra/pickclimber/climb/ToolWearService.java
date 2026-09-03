package dev.maicra.pickclimber.climb;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

final class ToolWearService {
    private ToolWearService() {
    }

    static boolean damageHand(ServerPlayer player, InteractionHand hand, ToolWearReason reason) {
        return damageHand(player, hand, reason, 1);
    }

    static boolean damageHand(
            ServerPlayer player,
            InteractionHand hand,
            ToolWearReason reason,
            int units
    ) {
        ItemStack stack = player.getItemInHand(hand);
        return damageStack(player, stack, hand, reason.amount(units));
    }

    static boolean damageEquipped(ServerPlayer player, UUID toolId, ToolWearReason reason) {
        return damageEquipped(player, toolId, reason, 1);
    }

    static boolean damageEquipped(
            ServerPlayer player,
            UUID toolId,
            ToolWearReason reason,
            int units
    ) {
        ItemStack stack = ToolLocator.findEquipped(player, toolId);
        if (stack.isEmpty()) {
            return false;
        }

        InteractionHand hand = ToolIdentity.matches(player.getMainHandItem(), toolId)
                ? InteractionHand.MAIN_HAND
                : InteractionHand.OFF_HAND;
        return damageStack(player, stack, hand, reason.amount(units));
    }

    static boolean damageEquipped(ServerPlayer player, UUID toolId, int amount) {
        ItemStack stack = ToolLocator.findEquipped(player, toolId);
        if (stack.isEmpty()) {
            return false;
        }

        InteractionHand hand = ToolIdentity.matches(player.getMainHandItem(), toolId)
                ? InteractionHand.MAIN_HAND
                : InteractionHand.OFF_HAND;
        return damageStack(player, stack, hand, amount);
    }

    private static boolean damageStack(
            ServerPlayer player,
            ItemStack stack,
            InteractionHand hand,
            int baseAmount
    ) {
        if (stack.isEmpty()) {
            return false;
        }

        int multiplierPercent = ClimbRulesBridge.durabilityMultiplierPercent(player);
        long policyRevision = ClimbRulesBridge.durabilityPolicyRevision(player);
        int amount = ToolWearState.scaleDamage(stack, baseAmount, multiplierPercent, policyRevision);
        if (amount <= 0) {
            return true;
        }

        EquipmentSlot slot = hand == InteractionHand.MAIN_HAND
                ? EquipmentSlot.MAINHAND
                : EquipmentSlot.OFFHAND;
        stack.hurtAndBreak(
                amount,
                player.serverLevel(),
                player,
                brokenItem -> player.onEquippedItemBroken(brokenItem, slot)
        );
        return !stack.isEmpty();
    }
}
