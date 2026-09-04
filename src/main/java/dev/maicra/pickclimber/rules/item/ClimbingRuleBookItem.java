package dev.maicra.pickclimber.rules.item;

import dev.maicra.pickclimber.rules.network.ClimbingRuleBookViewerNetworking;
import dev.maicra.pickclimber.rules.RuleBookActivationMode;
import dev.maicra.pickclimber.rules.RuleBookScope;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public final class ClimbingRuleBookItem extends Item {
    public ClimbingRuleBookItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        return ClimbingRuleBookData.readReference(stack)
                .<Component>map(reference -> Component.literal(reference.bookName()))
                .orElseGet(() -> super.getName(stack));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!ClimbingRuleBookData.hasCurrentSchema(stack)) {
            return InteractionResultHolder.pass(stack);
        }
        if (level.isClientSide()) {
            ClimbingRuleBookViewerNetworking.request(hand);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        ClimbingRuleBookData.readReference(stack).ifPresentOrElse(reference -> {
            tooltip.add(Component.translatable("tooltip.pickclimber.rule_book.blocks", reference.assignedBlocks())
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable(
                    "tooltip.pickclimber.rule_book.durability", reference.pickaxeWear()
            ).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable(
                    reference.playerMiningEnabled()
                            ? "tooltip.pickclimber.rule_book.mining_enabled"
                            : "tooltip.pickclimber.rule_book.mining_disabled"
            ).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable(
                    reference.unmineableTerminals()
                            ? "tooltip.pickclimber.rule_book.terminals_locked"
                            : "tooltip.pickclimber.rule_book.terminals_mineable"
            ).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable(
                    reference.activationMode() == RuleBookActivationMode.PERMANENT
                            ? "tooltip.pickclimber.rule_book.permanent"
                            : "tooltip.pickclimber.rule_book.temporary",
                    reference.durationSeconds()
            ).withStyle(ChatFormatting.DARK_GRAY));
            if (!reference.authorName().isBlank()) {
                tooltip.add(Component.literal("Author: " + reference.authorName()).withStyle(ChatFormatting.DARK_GRAY));
            }
            if (reference.activationMode() == RuleBookActivationMode.TEMPORARY) {
                tooltip.add(Component.translatable(
                        reference.scope() == RuleBookScope.WORLD
                                ? "tooltip.pickclimber.rule_book.scope_world"
                                : "tooltip.pickclimber.rule_book.scope_player"
                ).withStyle(ChatFormatting.DARK_GRAY));
            }
        }, () -> tooltip.add(Component.translatable("tooltip.pickclimber.rule_book.invalid")
                .withStyle(ChatFormatting.RED)));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
