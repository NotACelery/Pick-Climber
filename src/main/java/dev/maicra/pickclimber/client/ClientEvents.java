package dev.maicra.pickclimber.client;

import dev.maicra.pickclimber.PickClimber;
import dev.maicra.pickclimber.climb.ClimbManager;
import dev.maicra.pickclimber.climb.ClimbingHandSelector;
import dev.maicra.pickclimber.network.DetachRequestPayload;
import dev.maicra.pickclimber.network.SlideInputPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = PickClimber.MOD_ID, value = Dist.CLIENT)
public final class ClientEvents {
    private static final ItemStack RANGE_ICON = new ItemStack(Items.STRING);
    private static boolean hadPlayerLastTick;
    private static boolean wasAttachedLastTick;
    private static boolean jumpWasDown;
    private static boolean jumpReleaseArmed;
    private static boolean shiftWasDown;
    private static long lastShiftPressGameTime = Long.MIN_VALUE;
    private static final int DOUBLE_SHIFT_WINDOW_TICKS = 7;

    private ClientEvents() {
    }


    @SubscribeEvent
    public static void onClientLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        Player player = event.getPlayer();
        if (player != null) {
            // Se ejecuta mientras el ClientLevel todavía existe. El paquete de
            // limpieza del servidor puede perderse durante el cierre de conexión,
            // así que eliminamos también el overlay sintético localmente.
            ClimbManager.clearAllClientStates(player);
        } else {
            ClimbManager.clearAllClientStates(null);
        }
        hadPlayerLastTick = false;
        resetJumpLatch();
    }

    @SubscribeEvent
    public static void onInteractionKey(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;

        if (player == null || !event.isAttack() || !ClimbManager.isAttached(player)) {
            return;
        }

        if (ClimbManager.activeHand(player) != InteractionHand.MAIN_HAND) {
            // El ancla de la secundaria no secuestra el clic izquierdo. La mano
            // principal puede minar, atacar y reproducir su swing vanilla.
            return;
        }

        // Regla intencional: usar para minar/atacar el mismo pico clavado en la
        // principal lo retira del ancla. La mano libre no es interceptada.
        event.setSwingHand(false);
        event.setCanceled(true);
        ClimbManager.detachClient(player, false);
        PacketDistributor.sendToServer(new DetachRequestPayload(false));
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;

        if (player == null) {
            if (hadPlayerLastTick) {
                ClimbManager.clearAllClientStates(null);
            }
            hadPlayerLastTick = false;
            resetJumpLatch();
            return;
        }

        hadPlayerLastTick = true;
        boolean attached = ClimbManager.isAttached(player);
        boolean jumpDown = minecraft.options.keyJump.isDown();
        boolean shiftDown = minecraft.options.keyShift.isDown();

        if (!attached) {
            // Fuera del anclaje solo seguimos el estado físico actual de la tecla.
            // No consumimos la cola interna de KeyMapping, porque puede contener
            // clicks antiguos de saltos vanilla y producir un wall jump fantasma
            // al engancharse varios ticks después.
            wasAttachedLastTick = false;
            jumpWasDown = jumpDown;
            jumpReleaseArmed = false;
            shiftWasDown = shiftDown;
            lastShiftPressGameTime = Long.MIN_VALUE;
            return;
        }

        PacketDistributor.sendToServer(new SlideInputPayload(
                minecraft.player.input.forwardImpulse,
                minecraft.player.input.leftImpulse,
                player.getYRot(),
                player.getXRot()
        ));

        if (!wasAttachedLastTick) {
            // Al recibir un anclaje nuevo se exige una liberación completa de la
            // tecla. Esto impide que el mismo Espacio usado para saltar/impulsarse
            // o un click almacenado desenganche inmediatamente al jugador.
            wasAttachedLastTick = true;
            jumpWasDown = jumpDown;
            jumpReleaseArmed = !jumpDown;
            shiftWasDown = shiftDown;
            lastShiftPressGameTime = Long.MIN_VALUE;
            return;
        }

        boolean freshShiftPress = shiftDown && !shiftWasDown;
        shiftWasDown = shiftDown;
        if (freshShiftPress) {
            long now = player.level().getGameTime();
            if (lastShiftPressGameTime != Long.MIN_VALUE
                    && now - lastShiftPressGameTime <= DOUBLE_SHIFT_WINDOW_TICKS) {
                lastShiftPressGameTime = Long.MIN_VALUE;
                ClimbManager.detachClient(player, false);
                PacketDistributor.sendToServer(new DetachRequestPayload(false));
                return;
            }
            lastShiftPressGameTime = now;
        }

        if (!jumpDown) {
            jumpReleaseArmed = true;
        }

        boolean freshJumpPress = jumpReleaseArmed && jumpDown && !jumpWasDown;
        jumpWasDown = jumpDown;

        if (!freshJumpPress) {
            return;
        }

        jumpReleaseArmed = false;
        ClimbManager.detachClient(player, true);
        PacketDistributor.sendToServer(new DetachRequestPayload(true));
    }


    private static void resetJumpLatch() {
        wasAttachedLastTick = false;
        jumpWasDown = false;
        jumpReleaseArmed = false;
        shiftWasDown = false;
        lastShiftPressGameTime = Long.MIN_VALUE;
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.options.hideGui) {
            return;
        }

        GuiGraphics gui = event.getGuiGraphics();
        renderReachIndicator(minecraft, player, gui);
    }

    private static void renderReachIndicator(Minecraft minecraft, Player player, GuiGraphics gui) {
        if (!(minecraft.hitResult instanceof BlockHitResult hit)) {
            return;
        }

        InteractionHand preferredHand = ClimbingHandSelector.preferred(player, hit);
        if (preferredHand == null) {
            return;
        }

        int x = gui.guiWidth() / 2 - 8;
        int y = gui.guiHeight() / 2 + 10;
        gui.renderItem(RANGE_ICON, x, y);
    }

}
