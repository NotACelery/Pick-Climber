package dev.maicra.pickclimber.climb;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

final class ClimbRuntimeTicker {
    private ClimbRuntimeTicker() {
    }

    static void tick(Player player) {
        if (player.level().isClientSide()) {
            ClientClimbSynchronizer.tick(player);
        } else if (player instanceof ServerPlayer serverPlayer) {
            tickServer(serverPlayer);
        }
    }

    private static void tickServer(ServerPlayer player) {
        if (player.onGround()) {
            JumpTracker.clear(player);
        }

        ServerClimbState state = ClimbStateStore.server(player.getUUID());
        if (state == null) {
            return;
        }

        state = AnchorStateValidator.reconcileActiveHand(player, state);
        if (!AnchorStateValidator.isValid(player, state)) {
            boolean failedImmediately = player.level().getGameTime() - state.attachedAtGameTime()
                    <= ClimbTuning.FAILED_ATTACH_GRACE_TICKS;
            AnchorLifecycle.detachServer(player, false, failedImmediately);
            return;
        }

        if (state.anchorFace() == Direction.DOWN
                && player.level().getGameTime() - state.attachedAtGameTime() > 0
                && (player.level().getGameTime() - state.attachedAtGameTime())
                % ClimbTuning.CEILING_DURABILITY_INTERVAL_TICKS == 0
                && !ToolWearService.damageEquipped(
                        player,
                        state.toolId(),
                        ToolWearReason.CEILING_SUSTAINED
                )) {
            AnchorLifecycle.detachServer(player, false);
            return;
        }

        if (player.getAbilities().flying) {
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }

        state = AnchorMotionService.advance(player, state);
        if (state == null) {
            AnchorLifecycle.detachServer(player, false);
            return;
        }
        ClimbStateStore.putServer(player.getUUID(), state);

        if (player.tickCount % ClimbTuning.CRACK_REFRESH_INTERVAL == 0) {
            AnchorVisualService.showCracks(player.serverLevel(), state);
        }
        if (state.anchorFace() == Direction.DOWN
                || state.motion() != AnchorMotion.FIXED
                || player.tickCount % ClimbTuning.SERVER_SYNC_INTERVAL == 0) {
            ClimbSynchronization.sendAttached(player, state, false);
        }
        if (state.anchorFace() == Direction.DOWN
                && player.tickCount % ClimbTuning.REMOTE_POSE_REFRESH_INTERVAL_TICKS == 0) {
            ClimbSynchronization.sendRemotePose(player, state);
        }

        AnchorPositioning.holdPlayer(player, state);
    }
}
