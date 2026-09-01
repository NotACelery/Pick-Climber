package dev.maicra.pickclimber.client;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.maicra.pickclimber.PickClimber;
import dev.maicra.pickclimber.climb.AnchorIndicatorStatus;
import dev.maicra.pickclimber.climb.ClimbManager;
import dev.maicra.pickclimber.network.DetachRequestPayload;
import dev.maicra.pickclimber.network.SlideInputPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
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
    private static final int RANGE_ICON_SIZE = 16;
    private static final int RANGE_ICON_Y_OFFSET = 10;
    private static final int OPAQUE_ALPHA = 0xFF000000;
    private static final int DOUBLE_SHIFT_WINDOW_TICKS = 7;

    private static boolean hadPlayerLastTick;
    private static boolean wasAttachedLastTick;
    private static boolean jumpWasDown;
    private static boolean jumpReleaseArmed;
    private static boolean shiftWasDown;
    private static long lastShiftPressGameTime = Long.MIN_VALUE;

    private ClientEvents() {
    }

    @SubscribeEvent
    public static void onClientLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        Player player = event.getPlayer();
        if (player != null) {
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
            return;
        }

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
        if (!(minecraft.hitResult instanceof BlockHitResult hit)
                || hit.getType() != HitResult.Type.BLOCK) {
            return;
        }

        AnchorIndicatorStatus status = ClimbManager.anchorIndicatorStatus(player, hit);
        if (status == AnchorIndicatorStatus.NONE) {
            return;
        }

        int iconX = gui.guiWidth() / 2 - RANGE_ICON_SIZE / 2;
        int iconY = gui.guiHeight() / 2 + RANGE_ICON_Y_OFFSET;
        int color = status.color();

        renderTintedRangeIcon(gui, iconX, iconY, color);
        renderRangeBorder(gui, iconX, iconY, OPAQUE_ALPHA | color);
    }

    private static void renderTintedRangeIcon(GuiGraphics gui, int x, int y, int color) {
        gui.flush();

        RenderSystem.setShaderColor(
                colorChannel(color, 16),
                colorChannel(color, 8),
                colorChannel(color, 0),
                1.0F
        );

        try {
            gui.renderItem(RANGE_ICON, x, y);
            gui.flush();
        } finally {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    private static void renderRangeBorder(GuiGraphics gui, int x, int y, int color) {
        int right = x + RANGE_ICON_SIZE;
        int bottom = y + RANGE_ICON_SIZE;

        gui.fill(x - 1, y - 1, right + 1, y, color);
        gui.fill(x - 1, bottom, right + 1, bottom + 1, color);
        gui.fill(x - 1, y, x, bottom, color);
        gui.fill(right, y, right + 1, bottom, color);
    }

    private static float colorChannel(int color, int shift) {
        return ((color >> shift) & 0xFF) / 255.0F;
    }
}
