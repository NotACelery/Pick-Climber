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
            // This runs while ClientLevel still exists. The server cleanup packet
            // may be lost while the connection closes, so also remove the
            // synthetic overlay locally.
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
            // An off-hand anchor does not capture left click. The main hand may
            // mine, attack, and play its vanilla swing.
            return;
        }

        // Intentional rule: mining or attacking with the same pinned main-hand
        // pickaxe removes it from the anchor. The free hand is not intercepted.
        event.setSwingHand(false);
        event.setCanceled(true);
        ClimbManager.detachClient(player, false);
        PacketDistributor.sendToServer(detachRequest(minecraft, false));
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
            // Outside an anchor, only track the key's current physical state.
            // Do not consume KeyMapping's internal queue: it may contain stale
            // vanilla jump clicks and cause a phantom wall jump when attaching
            // several ticks later.
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
            // A newly received anchor requires a full key release. This prevents
            // the same Space press used to jump or boost, or a queued click, from
            // immediately detaching the player.
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
                PacketDistributor.sendToServer(detachRequest(minecraft, false));
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
        PacketDistributor.sendToServer(detachRequest(minecraft, true));
    }

    private static DetachRequestPayload detachRequest(Minecraft minecraft, boolean jump) {
        Player player = minecraft.player;
        if (player == null) {
            return new DetachRequestPayload(jump, 0.0F, 0.0F, 0.0F, 0.0F);
        }
        return new DetachRequestPayload(
                jump,
                minecraft.player.input.forwardImpulse,
                minecraft.player.input.leftImpulse,
                player.getYRot(),
                player.getXRot()
        );
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
