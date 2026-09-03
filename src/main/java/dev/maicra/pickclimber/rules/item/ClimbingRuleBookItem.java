package dev.maicra.pickclimber.rules.item;

import dev.maicra.pickclimber.rules.ClimbingRulesClientUi;
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
        var definition = ClimbingRuleBookData.readDefinitionValidated(stack);
        if (definition.isPresent()) {
            return Component.literal(definition.get().bookName());
        }
        return super.getName(stack);
    }


    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        var definition = ClimbingRuleBookData.readDefinitionValidated(stack);
        if (definition.isEmpty()) {
            return InteractionResultHolder.pass(stack);
        }
        if (level.isClientSide()) {
            ClimbingRulesClientUi.openViewer(definition.get());
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
        ClimbingRuleBookData.readDefinitionValidated(stack).ifPresentOrElse(definition -> {
            var profile = definition.profile();
            int assigned = profile.stableBlocks().size()
                    + profile.unstableBlocks().size()
                    + profile.unclimbableBlocks().size();
            tooltip.add(Component.translatable("tooltip.pickclimber.rule_book.blocks", assigned)
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable(
                    "tooltip.pickclimber.rule_book.durability",
                    profile.durabilityMultiplierPercent()
            ).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable(
                    profile.playerMiningEnabled()
                            ? "tooltip.pickclimber.rule_book.mining_enabled"
                            : "tooltip.pickclimber.rule_book.mining_disabled"
            ).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable(
                    profile.unmineableTerminals()
                            ? "tooltip.pickclimber.rule_book.terminals_locked"
                            : "tooltip.pickclimber.rule_book.terminals_mineable"
            ).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable(
                    definition.activationMode() == RuleBookActivationMode.PERMANENT
                            ? "tooltip.pickclimber.rule_book.permanent"
                            : "tooltip.pickclimber.rule_book.temporary",
                    definition.durationSeconds()
            ).withStyle(ChatFormatting.DARK_GRAY));
            if (definition.activationMode() == RuleBookActivationMode.TEMPORARY) {
                tooltip.add(Component.translatable(
                        definition.scope() == RuleBookScope.WORLD
                                ? "tooltip.pickclimber.rule_book.scope_world"
                                : "tooltip.pickclimber.rule_book.scope_player"
                ).withStyle(ChatFormatting.DARK_GRAY));
            }
        }, () -> tooltip.add(Component.translatable("tooltip.pickclimber.rule_book.invalid")
                .withStyle(ChatFormatting.RED)));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
