package dev.maicra.pickclimber.client;

import dev.maicra.pickclimber.climb.ClimbManager;
import dev.maicra.pickclimber.climb.ClimbRuntimeGate;
import dev.maicra.pickclimber.network.DetachRequestPayload;
import dev.maicra.pickclimber.network.SlideInputPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

final class ClientClimbInputController {
    private static boolean hadPlayerLastTick;
    private static boolean wasAttachedLastTick;
    private static boolean jumpWasDown;
    private static boolean jumpReleaseArmed;
    private static boolean shiftWasDown;
    private static long lastShiftPressGameTime = Long.MIN_VALUE;

    private ClientClimbInputController() {
    }

    static void onLogout(Player player) {
        ClimbManager.clearAllClientStates(player);
        hadPlayerLastTick = false;
        resetInputState();
    }

    static boolean handleAttack(Minecraft minecraft) {
        LocalPlayer player = minecraft.player;
        if (player == null
                || !ClimbRuntimeGate.interactionsEnabled(player)
                || !ClimbManager.isAttached(player)
                || ClimbManager.activeHand(player) != InteractionHand.MAIN_HAND) {
            return false;
        }

        ClimbManager.detachClient(player, false);
        PacketDistributor.sendToServer(detachRequest(minecraft, false));
        return true;
    }

    static void tick(Minecraft minecraft) {
        LocalPlayer player = minecraft.player;
        if (player == null) {
            if (hadPlayerLastTick) {
                ClimbManager.clearAllClientStates(null);
            }
            hadPlayerLastTick = false;
            resetInputState();
            return;
        }

        hadPlayerLastTick = true;
        if (!ClimbRuntimeGate.interactionsEnabled(player)) {
            resetAttachmentInput(minecraft, player);
            return;
        }

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
                player.input.forwardImpulse,
                player.input.leftImpulse,
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

        if (handleDoubleShiftDetach(minecraft, player, shiftDown)) {
            return;
        }
        handleJumpDetach(minecraft, player, jumpDown);
    }

    private static boolean handleDoubleShiftDetach(
            Minecraft minecraft,
            Player player,
            boolean shiftDown
    ) {
        boolean freshShiftPress = shiftDown && !shiftWasDown;
        shiftWasDown = shiftDown;
        if (!freshShiftPress) {
            return false;
        }

        long now = player.level().getGameTime();
        if (lastShiftPressGameTime != Long.MIN_VALUE
                && now - lastShiftPressGameTime <= ClientClimbDefaults.DOUBLE_SHIFT_WINDOW_TICKS) {
            lastShiftPressGameTime = Long.MIN_VALUE;
            ClimbManager.detachClient(player, false);
            PacketDistributor.sendToServer(detachRequest(minecraft, false));
            return true;
        }
        lastShiftPressGameTime = now;
        return false;
    }

    private static void handleJumpDetach(Minecraft minecraft, Player player, boolean jumpDown) {
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
        LocalPlayer player = minecraft.player;
        if (player == null) {
            return new DetachRequestPayload(jump, 0.0F, 0.0F, 0.0F, 0.0F);
        }
        return new DetachRequestPayload(
                jump,
                player.input.forwardImpulse,
                player.input.leftImpulse,
                player.getYRot(),
                player.getXRot()
        );
    }

    private static void resetAttachmentInput(Minecraft minecraft, Player player) {
        wasAttachedLastTick = false;
        jumpWasDown = minecraft.options.keyJump.isDown();
        jumpReleaseArmed = false;
        shiftWasDown = minecraft.options.keyShift.isDown();
        lastShiftPressGameTime = Long.MIN_VALUE;
    }

    private static void resetInputState() {
        wasAttachedLastTick = false;
        jumpWasDown = false;
        jumpReleaseArmed = false;
        shiftWasDown = false;
        lastShiftPressGameTime = Long.MIN_VALUE;
    }
}
