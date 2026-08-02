package dev.maicra.pickclimber.client;

import dev.maicra.pickclimber.PickClimber;
import dev.maicra.pickclimber.climb.ClimbManager;
import dev.maicra.pickclimber.climb.ToolIdentity;
import dev.maicra.pickclimber.network.DetachRequestPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
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

    private ClientEvents() {
    }

    @SubscribeEvent
    public static void onInteractionKey(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;

        if (player == null || !event.isAttack() || !ClimbManager.isAttached(player)) {
            return;
        }

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

        if (!attached) {
            // Fuera del anclaje solo seguimos el estado físico actual de la tecla.
            // No consumimos la cola interna de KeyMapping, porque puede contener
            // clicks antiguos de saltos vanilla y producir un wall jump fantasma
            // al engancharse varios ticks después.
            wasAttachedLastTick = false;
            jumpWasDown = jumpDown;
            jumpReleaseArmed = false;
            return;
        }

        if (!wasAttachedLastTick) {
            // Al recibir un anclaje nuevo se exige una liberación completa de la
            // tecla. Esto impide que el mismo Espacio usado para saltar/impulsarse
            // o un click almacenado desenganche inmediatamente al jugador.
            wasAttachedLastTick = true;
            jumpWasDown = jumpDown;
            jumpReleaseArmed = !jumpDown;
            return;
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
        renderHeldToolIndicators(player, gui);
    }

    private static void renderReachIndicator(Minecraft minecraft, Player player, GuiGraphics gui) {
        if (!(minecraft.hitResult instanceof BlockHitResult hit)) {
            return;
        }

        boolean mainReady = ClimbManager.canAttemptAnchor(player, InteractionHand.MAIN_HAND, hit);
        boolean offReady = ClimbManager.canAttemptAnchor(player, InteractionHand.OFF_HAND, hit);

        if (!mainReady && !offReady) {
            return;
        }

        int x = gui.guiWidth() / 2 - 8;
        int y = gui.guiHeight() / 2 + 10;
        gui.renderItem(RANGE_ICON, x, y);
    }

    private static void renderHeldToolIndicators(Player player, GuiGraphics gui) {
        int centerX = gui.guiWidth() / 2;
        int itemY = gui.guiHeight() - 19;
        long gameTime = player.level().getGameTime();
        InteractionHand active = ClimbManager.activeHand(player);

        ItemStack main = player.getMainHandItem();
        if (ClimbManager.isPickaxe(main)) {
            int itemX = centerX - 90 + player.getInventory().selected * 20 + 2;
            renderSlotOverlay(gui, itemX, itemY, main, gameTime, active == InteractionHand.MAIN_HAND);
        }

        ItemStack off = player.getOffhandItem();
        if (ClimbManager.isPickaxe(off)) {
            int itemX = player.getMainArm() == HumanoidArm.RIGHT
                    ? centerX - 117
                    : centerX + 101;
            renderSlotOverlay(gui, itemX, itemY, off, gameTime, active == InteractionHand.OFF_HAND);
        }
    }

    private static void renderSlotOverlay(
            GuiGraphics gui,
            int x,
            int y,
            ItemStack stack,
            long gameTime,
            boolean active
    ) {
        if (active) {
            // El pico que sostiene al jugador se muestra como un cooldown lleno y
            // congelado. Se usa la posición exacta del icono vanilla (16x16), no
            // la esquina del fondo de la hotbar.
            gui.fill(x, y, x + 16, y + 16, 0xB0000000);
            gui.fill(x - 1, y - 1, x + 17, y, 0xFFFFFFFF);
            gui.fill(x - 1, y + 16, x + 17, y + 17, 0xFFFFFFFF);
            gui.fill(x - 1, y, x, y + 16, 0xFFFFFFFF);
            gui.fill(x + 16, y, x + 17, y + 16, 0xFFFFFFFF);
            return;
        }

        float cooldown = ToolIdentity.cooldownFraction(
                stack,
                gameTime,
                ClimbManager.ANCHOR_COOLDOWN_TICKS
        );

        if (cooldown <= 0.0F) {
            return;
        }

        int height = Math.max(1, (int) Math.ceil(16.0F * cooldown));
        gui.fill(x, y + 16 - height, x + 16, y + 16, 0x99000000);
    }
}
