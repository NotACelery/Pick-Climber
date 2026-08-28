package dev.maicra.pickclimber.event;

import dev.maicra.pickclimber.PickClimber;
import dev.maicra.pickclimber.climb.ClimbManager;
import dev.maicra.pickclimber.climb.ClimbingHandSelector;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = PickClimber.MOD_ID)
public final class CommonEvents {
    private CommonEvents() {
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer
                && ClimbManager.isAttached(serverPlayer)
                && !ClimbManager.isAttachmentCoherent(serverPlayer)) {
            ClimbManager.recoverStaleAttachment(serverPlayer);
        }

        Player player = event.getEntity();
        if (!player.isSecondaryUseActive()) {
            return;
        }

        InteractionHand preferredHand = ClimbingHandSelector.preferred(player, event.getHitVec());
        if (preferredHand == null) {
            return;
        }

        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);

        if (player instanceof ServerPlayer serverPlayer) {
            ClimbManager.useClimbingTool(serverPlayer, preferredHand, event.getHitVec());
        }
    }

    @SubscribeEvent
    public static void onUseItemOnBlock(UseItemOnBlockEvent event) {
        if (event.getUsePhase() != UseItemOnBlockEvent.UsePhase.ITEM_AFTER_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        if (player == null) {
            return;
        }

        var context = event.getUseOnContext();
        BlockHitResult hit = new BlockHitResult(
                context.getClickLocation(),
                context.getClickedFace(),
                context.getClickedPos(),
                context.isInside()
        );

        InteractionHand hand = event.getHand();

        if (hand == InteractionHand.MAIN_HAND
                && ClimbManager.canAttemptAnchor(player, InteractionHand.OFF_HAND, hit)) {
            return;
        }

        if (!ClimbManager.canAttemptAnchor(player, hand, hit)) {
            if (player.level().isClientSide() && hand == InteractionHand.MAIN_HAND) {
                Component feedback = ClimbManager.anchorAttemptFailureMessage(player, hit);
                if (feedback != null) {
                    player.displayClientMessage(feedback, true);
                }
            }
            return;
        }

        event.cancelWithResult(ItemInteractionResult.SUCCESS);

        if (player instanceof ServerPlayer serverPlayer) {
            ClimbManager.useClimbingTool(serverPlayer, hand, hit);
        }
    }

    @SubscribeEvent
    public static void onRightClickEntity(PlayerInteractEvent.EntityInteractSpecific event) {
        if (!event.getEntity().level().isClientSide()
                || !ClimbManager.isClimbingTool(event.getItemStack())
                || event.getHand() == InteractionHand.OFF_HAND
                && ClimbManager.isClimbingTool(event.getEntity().getMainHandItem())) {
            return;
        }
        event.getEntity().displayClientMessage(
                Component.translatable("message.pickclimber.anchor.entity"),
                true
        );
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (!ClimbManager.isAttached(event.getEntity())
                || ClimbManager.activeHand(event.getEntity()) != InteractionHand.MAIN_HAND) {
            return;
        }

        event.setCanceled(true);
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            ClimbManager.detachServer(serverPlayer, false);
        }
    }

    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            ClimbManager.recordRealJump(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        ClimbManager.tick(event.getEntity());
    }

    @SubscribeEvent
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            ClimbManager.detachServer(serverPlayer, false);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            ClimbManager.cleanupServer(serverPlayer);
        }
    }
}
