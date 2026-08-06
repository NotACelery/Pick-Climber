package dev.maicra.pickclimber.event;

import dev.maicra.pickclimber.PickClimber;
import dev.maicra.pickclimber.climb.ClimbManager;
import dev.maicra.pickclimber.climb.ClimbingHandSelector;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = PickClimber.MOD_ID)
public final class CommonEvents {
    private CommonEvents() {
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        // Only the server may declare an anchor incoherent and clean it up.
        // Recover before selecting a hand so decisions are not made against a
        // physical state that no longer exists.
        if (event.getEntity() instanceof ServerPlayer serverPlayer
                && ClimbManager.isAttached(serverPlayer)
                && !ClimbManager.isAttachmentCoherent(serverPlayer)) {
            ClimbManager.recoverStaleAttachment(serverPlayer);
        }

        InteractionHand preferredHand = ClimbingHandSelector.preferred(
                event.getEntity(),
                event.getHitVec()
        );

        if (preferredHand == null || event.getHand() != preferredHand) {
            // Not cancelling is intentional. In particular, when the off hand is
            // preferred, the main hand gets its full interaction first. If it
            // places or uses something, the pipeline ends; if it returns PASS,
            // Minecraft continues and fires this event for the off hand.
            // On menu blocks, ClimbingHandSelector also returns null unless the
            // player holds Shift, preserving vanilla use.
            return;
        }

        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);

        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            ClimbManager.useClimbingTool(serverPlayer, preferredHand, event.getHitVec());
        }
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
