package dev.maicra.pickclimber.event;

import dev.maicra.pickclimber.PickClimber;
import dev.maicra.pickclimber.climb.AnchorInteractionService;
import dev.maicra.pickclimber.climb.AnchorUseDecision;
import dev.maicra.pickclimber.climb.ClimbManager;
import dev.maicra.pickclimber.climb.ClimbPresentationGate;
import dev.maicra.pickclimber.climb.ClimbRuntimeGate;
import dev.maicra.pickclimber.climb.PlayerClimbPresentationPreferences;
import dev.maicra.pickclimber.climb.PlayerClimbRuntimePreferences;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
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
        Player player = event.getEntity();
        if (!ClimbRuntimeGate.interactionsEnabled(player)) {
            return;
        }
        recoverStaleAttachment(player);
        if (!player.isSecondaryUseActive()) {
            return;
        }

        AnchorUseDecision decision = AnchorInteractionService.forcedAnchor(player, event.getHitVec());
        if (!decision.consume()) {
            return;
        }

        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
        performServerAnchor(player, decision.hand(), event.getHitVec());
    }

    @SubscribeEvent
    public static void onUseItemOnBlock(UseItemOnBlockEvent event) {
        if (event.getUsePhase() != UseItemOnBlockEvent.UsePhase.ITEM_AFTER_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        if (player == null || !ClimbRuntimeGate.interactionsEnabled(player)) {
            return;
        }

        var context = event.getUseOnContext();
        BlockHitResult hit = new BlockHitResult(
                context.getClickLocation(),
                context.getClickedFace(),
                context.getClickedPos(),
                context.isInside()
        );
        AnchorUseDecision decision = AnchorInteractionService.afterBlockUse(
                player,
                event.getHand(),
                hit
        );
        if (decision.feedback() != null && ClimbPresentationGate.showFailureText(player)) {
            player.displayClientMessage(decision.feedback(), true);
        }
        if (!decision.consume()) {
            return;
        }

        event.cancelWithResult(ItemInteractionResult.SUCCESS);
        performServerAnchor(player, decision.hand(), hit);
    }

    @SubscribeEvent
    public static void onRightClickEntity(PlayerInteractEvent.EntityInteractSpecific event) {
        if (!ClimbRuntimeGate.interactionsEnabled(event.getEntity())
                || !event.getEntity().level().isClientSide()
                || !ClimbManager.isClimbingTool(event.getItemStack())
                || event.getHand() == InteractionHand.OFF_HAND
                && ClimbManager.isClimbingTool(event.getEntity().getMainHandItem())) {
            return;
        }
        if (ClimbPresentationGate.showFailureText(event.getEntity())) {
            event.getEntity().displayClientMessage(
                    net.minecraft.network.chat.Component.translatable("message.pickclimber.anchor.entity"),
                    true
            );
        }
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (!ClimbRuntimeGate.interactionsEnabled(event.getEntity())
                || !ClimbManager.isAttached(event.getEntity())
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
        if (event.getEntity() instanceof ServerPlayer serverPlayer
                && ClimbRuntimeGate.interactionsEnabled(serverPlayer)) {
            ClimbManager.recordRealJump(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!ClimbRuntimeGate.interactionsEnabled(player)) {
            if (player instanceof ServerPlayer serverPlayer && ClimbManager.isAttached(serverPlayer)) {
                ClimbManager.detachServer(serverPlayer, false);
            }
            return;
        }
        ClimbManager.tick(player);
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
            PlayerClimbRuntimePreferences.clear(serverPlayer);
            PlayerClimbPresentationPreferences.clear(serverPlayer);
        }
    }

    private static void recoverStaleAttachment(Player player) {
        if (player instanceof ServerPlayer serverPlayer
                && ClimbManager.isAttached(serverPlayer)
                && !ClimbManager.isAttachmentCoherent(serverPlayer)) {
            ClimbManager.recoverStaleAttachment(serverPlayer);
        }
    }

    private static void performServerAnchor(
            Player player,
            InteractionHand hand,
            BlockHitResult hit
    ) {
        if (player instanceof ServerPlayer serverPlayer) {
            ClimbManager.useClimbingTool(serverPlayer, hand, hit);
        }
    }
}
