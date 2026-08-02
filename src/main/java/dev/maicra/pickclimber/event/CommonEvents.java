package dev.maicra.pickclimber.event;

import dev.maicra.pickclimber.PickClimber;
import dev.maicra.pickclimber.climb.ClimbManager;
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
        InteractionHand hand = event.getHand();

        if (ClimbManager.isAttached(event.getEntity())
                && ClimbManager.activeHand(event.getEntity()) == hand) {
            // Solo el servidor puede declarar incoherente y limpiar un anclaje.
            // El cliente nunca modifica física ni estado por su cuenta.
            if (event.getEntity() instanceof ServerPlayer serverPlayer
                    && !ClimbManager.isAttachmentCoherent(serverPlayer)) {
                ClimbManager.recoverStaleAttachment(serverPlayer);
            } else {
                InteractionHand other = hand == InteractionHand.MAIN_HAND
                        ? InteractionHand.OFF_HAND
                        : InteractionHand.MAIN_HAND;

                // Si el otro pico está listo, se deja pasar esta mano para que
                // el pipeline vanilla pruebe la segunda mano.
                if (ClimbManager.canAttemptAnchor(event.getEntity(), other, event.getHitVec())) {
                    return;
                }

                // Tras terminar su cooldown, el mismo pico también puede mover
                // el anclaje cuando no existe un segundo pico disponible.
                if (!ClimbManager.canAttemptAnchor(event.getEntity(), hand, event.getHitVec())) {
                    return;
                }
            }
        }

        if (!ClimbManager.canAttemptAnchor(event.getEntity(), hand, event.getHitVec())) {
            return;
        }

        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);

        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            ClimbManager.useClimbingTool(serverPlayer, hand, event.getHitVec());
        }
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (!ClimbManager.isAttached(event.getEntity())) {
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
