package dev.maicra.pickclimber.rules.item;

import java.util.List;
import java.util.Optional;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import dev.maicra.pickclimber.rules.TemporaryRuleBookIssuanceService;

public final class TemporaryRuleBookItem extends Item {
    public TemporaryRuleBookItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("item.pickclimber.temporary_rule_book");
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        TemporaryRuleBookData.readDisplayMetadata(stack).ifPresentOrElse(data -> {
            tooltip.add(Component.translatable("tooltip.pickclimber.temporary_rule_book.owner_bound")
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal(data.bookName()).withStyle(ChatFormatting.DARK_GRAY));
        }, () -> tooltip.add(Component.translatable("tooltip.pickclimber.rule_book.invalid")
                .withStyle(ChatFormatting.RED)));
        super.appendHoverText(stack, context, tooltip, flag);
    }

    @Override
    public int getEntityLifespan(ItemStack stack, Level level) {
        Optional<TemporaryRuleBookData.TransportData> dataOptional = TemporaryRuleBookData.readValidated(stack);
        if (dataOptional.isEmpty()) {
            return 1;
        }
        TemporaryRuleBookData.TransportData data = dataOptional.get();
        if (data.startCounterOnPickup()
                && TemporaryRuleBookIssuanceService.isUnclaimed(data)) {
            return 20 * 60 * 5;
        }
        long remaining = data.expiresAtGameTime() - level.getGameTime();
        return (int) Math.max(1L, Math.min(Integer.MAX_VALUE, remaining));
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        if (entity.level().isClientSide()) {
            return false;
        }
        MinecraftServer server = entity.getServer();
        if (server == null) {
            return false;
        }
        Optional<TemporaryRuleBookData.TransportData> dataOptional = TemporaryRuleBookData.readValidated(stack);
        if (dataOptional.isEmpty()) {
            entity.discard();
            return true;
        }

        TemporaryRuleBookData.TransportData data = dataOptional.get();
        long gameTime = server.overworld().getGameTime();
        boolean active = TemporaryRuleBookIssuanceService.isActive(
                data.owner(),
                data.issuanceToken(),
                gameTime
        );
        if (TemporaryRuleBookData.isExpired(data, gameTime) || !active) {
            TemporaryRuleBookIssuanceService.releaseFromStack(stack, server);
            entity.discard();
            return true;
        }
        return false;
    }

    @Override
    public void onDestroyed(ItemEntity itemEntity, DamageSource damageSource) {
        if (!itemEntity.level().isClientSide() && itemEntity.getServer() != null) {
            TemporaryRuleBookIssuanceService.releaseFromStack(itemEntity.getItem(), itemEntity.getServer());
        }
    }
}
