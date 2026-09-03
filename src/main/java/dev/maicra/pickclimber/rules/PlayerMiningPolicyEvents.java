package dev.maicra.pickclimber.rules;

import dev.maicra.pickclimber.ModBlocks;
import dev.maicra.pickclimber.PickClimber;
import dev.maicra.pickclimber.climb.ClimbManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber(modid = PickClimber.MOD_ID)
public final class PlayerMiningPolicyEvents {
    private PlayerMiningPolicyEvents() {
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()
                || EffectiveClimbingRulesService.resolve(player).playerMiningEnabled()
                || MapmakerPermissions.canBypassMiningLock(player)) {
            return;
        }

        if (ClimbManager.isAttached(player) && ClimbManager.activeHand(player) == InteractionHand.MAIN_HAND) {
            return;
        }
        event.setCanceled(true);
        player.displayClientMessage(Component.translatable("message.pickclimber.rules.mining_disabled"), true);
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (!player.isCreative() && (event.getState().is(ModBlocks.CLIMBING_RULES_TABLE.get())
                || event.getState().is(ModBlocks.CLIMBING_RULE_DISPENSER.get()))) {
            event.setCanceled(true);
            return;
        }

        ClimbingRulesRuntimeView rules = EffectiveClimbingRulesService.resolve(player);
        if (!player.isCreative()
                && rules.unmineableTerminals()
                && event.getState().is(ModBlocks.CLIMBING_RULES_TERMINAL.get())) {
            event.setCanceled(true);
            return;
        }

        if (rules.playerMiningEnabled() || MapmakerPermissions.canBypassMiningLock(player)) {
            return;
        }
        event.setCanceled(true);
    }
}
