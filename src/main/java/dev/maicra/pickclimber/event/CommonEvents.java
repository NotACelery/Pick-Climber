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
        // Solo el servidor puede declarar incoherente y limpiar un anclaje. Se
        // recupera antes de seleccionar mano para no tomar decisiones sobre un
        // estado físico que ya dejó de existir.
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
            // No cancelar es intencional. En particular, cuando la secundaria
            // es la preferida, la interacción completa de la principal se prueba
            // primero. Si coloca/usa algo, el pipeline termina; si devuelve PASS,
            // Minecraft continúa y dispara este evento para la secundaria.
            // En bloques con menú, ClimbingHandSelector también devuelve null
            // salvo que el jugador mantenga Shift, preservando el uso vanilla.
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
            // Con el ancla en la secundaria, el clic izquierdo pertenece por
            // completo a la principal: minar y atacar no sueltan al jugador.
            return;
        }

        // Comportamiento intencional: intentar minar con el mismo pico que está
        // sosteniendo al jugador retira la herramienta del ancla. La otra mano
        // sigue pudiendo minar y atacar sin producir este detach.
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
