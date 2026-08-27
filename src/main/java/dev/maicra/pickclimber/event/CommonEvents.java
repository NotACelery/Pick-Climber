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
        // Keep stale-state recovery at the start of the normal interaction
        // pipeline, but never consume the click here. Blocks must get their own
        // useItemOn/useWithoutItem handling before Pick Climber considers an
        // anchor. This is important for modded machines that open menus without
        // exposing BlockState#getMenuProvider.
        if (event.getEntity() instanceof ServerPlayer serverPlayer
                && ClimbManager.isAttached(serverPlayer)
                && !ClimbManager.isAttachmentCoherent(serverPlayer)) {
            ClimbManager.recoverStaleAttachment(serverPlayer);
        }

        Player player = event.getEntity();
        if (!player.isSecondaryUseActive()) {
            return;
        }

        // Shift + RMB is the explicit force-anchor gesture. Resolve the preferred
        // hand here, before the block can consume the interaction. This is the
        // only path allowed to pre-empt a machine/chest/Farmer interaction.
        // If the target is not a genuinely valid anchor, preferred() returns null
        // and vanilla/modded Shift interaction proceeds untouched.
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

        // Preserve the original off-hand priority. If both hands can anchor, the
        // main-hand item gets its normal useOn opportunity and the interaction
        // pipeline proceeds to the off hand, where Pick Climber consumes it.
        if (hand == InteractionHand.MAIN_HAND
                && ClimbManager.canAttemptAnchor(player, InteractionHand.OFF_HAND, hit)) {
            return;
        }

        if (!ClimbManager.canAttemptAnchor(player, hand, hit)) {
            // Feedback belongs here, after block interaction passed. A chest,
            // Farmer, machine, etc. that consumed the click therefore opens
            // normally without a misleading Pick Climber warning.
            if (player.level().isClientSide() && hand == InteractionHand.MAIN_HAND) {
                Component feedback = ClimbManager.anchorAttemptFailureMessage(player, hit);
                if (feedback != null) {
                    player.displayClientMessage(feedback, true);
                }
            }
            return;
        }

        // ITEM_AFTER_BLOCK is only reached after the target block declined the
        // interaction. Pick Climber can now safely take ownership of this click.
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
            // With the anchor in the off hand, left click belongs entirely to
            // the main hand: mining and attacking do not detach the player.
            return;
        }

        // Intentional behavior: mining with the same pickaxe supporting the
        // player removes that tool from the anchor. The other hand can still
        // mine and attack without causing this detach.
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
