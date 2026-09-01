package dev.maicra.pickclimber.climb;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public final class ClientClimbSynchronizer {
    private ClientClimbSynchronizer() {
    }

    static void tick(Player player) {
        ClientClimbState state = ClimbStateStore.client(player.getUUID());
        if (state == null) {
            return;
        }

        long elapsed = player.level().getGameTime() - state.lastSyncGameTime();
        if (elapsed > ClimbTuning.CLIENT_SYNC_TIMEOUT_TICKS) {
            ClimbStateStore.removeClient(player.getUUID());
            AnchorVisualService.clearClientCracks(player, state);
        }
    }

    public static void applyAnchor(Player player, ClientAnchorSync sync) {
        ClientClimbState previousState = ClimbStateStore.client(player.getUUID());
        if (!sync.attached()) {
            applyDetach(player, previousState, sync);
            return;
        }

        if (previousState != null
                && (previousState.crackId() != sync.crackId()
                || !previousState.anchorBlock().equals(sync.anchorBlock()))) {
            AnchorVisualService.clearClientCracks(player, previousState);
        }

        ItemStack localTool = player.getItemInHand(sync.hand());
        if (ClimbingToolClassifier.isClimbingTool(localTool) && sync.newAnchor()) {
            if (!ToolIdentity.matches(localTool, sync.toolId())) {
                ToolIdentity.assign(localTool, sync.toolId());
            }
            AnchorCooldownService.startLocal(
                    localTool,
                    player.level().getGameTime(),
                    sync.cooldownTicks()
            );
        }

        long now = player.level().getGameTime();
        long poseStartedGameTime = sync.newAnchor()
                || previousState == null
                || !previousState.toolId().equals(sync.toolId())
                ? now
                : previousState.poseStartedGameTime();

        ClientClimbState next = new ClientClimbState(
                sync.target(),
                sync.anchorBlock(),
                sync.crackId(),
                sync.hand(),
                sync.toolId(),
                sync.restoreNoGravity(),
                sync.restoreFlying(),
                now,
                poseStartedGameTime,
                sync.ceilingAnchor()
        );
        ClimbStateStore.putClient(player.getUUID(), next);
        if (sync.ceilingAnchor()) {
            player.setDeltaMovement(Vec3.ZERO);
            player.setPos(sync.target());
        }
    }

    public static void applyBoost(
            Player player,
            Vec3 velocity,
            InteractionHand hand,
            int cooldownTicks
    ) {
        if (ClimbStateStore.hasClient(player.getUUID())) {
            return;
        }

        ItemStack localTool = player.getItemInHand(hand);
        if (ClimbingToolClassifier.isClimbingTool(localTool) && cooldownTicks > 0) {
            AnchorCooldownService.startLocal(
                    localTool,
                    player.level().getGameTime(),
                    cooldownTicks
            );
        }

        player.setDeltaMovement(velocity);
        player.fallDistance = 0.0F;
        player.setOnGround(false);
    }

    public static void applyRemotePose(
            Player receivingPlayer,
            UUID playerId,
            boolean ceilingAnchor,
            InteractionHand hand
    ) {
        if (!ceilingAnchor) {
            ClimbStateStore.removeRemotePose(playerId);
            return;
        }
        ClimbStateStore.putRemotePose(
                playerId,
                new RemoteAnchorPoseState(hand, receivingPlayer.level().getGameTime())
        );
    }

    static RemoteAnchorPoseState remotePose(Player player) {
        RemoteAnchorPoseState state = ClimbStateStore.remotePose(player.getUUID());
        if (state == null) {
            return null;
        }
        if (player.level().getGameTime() - state.lastSyncGameTime()
                > ClimbTuning.CLIENT_SYNC_TIMEOUT_TICKS) {
            ClimbStateStore.removeRemotePose(player.getUUID());
            return null;
        }
        return state;
    }

    private static void applyDetach(
            Player player,
            ClientClimbState previousState,
            ClientAnchorSync sync
    ) {
        ClimbStateStore.removeClient(player.getUUID());
        if (previousState != null) {
            AnchorVisualService.clearClientCracks(player, previousState);
        }
        ItemStack releasedTool = ToolLocator.findOwned(player, sync.toolId());
        if (!releasedTool.isEmpty()) {
            if (sync.refundCooldown()) {
                AnchorCooldownService.clearLocal(releasedTool);
            } else if (sync.cooldownTicks() > 0) {
                AnchorCooldownService.startLocalRemaining(
                        releasedTool,
                        player.level().getGameTime(),
                        sync.cooldownTicks()
                );
            } else {
                AnchorCooldownService.clearLocal(releasedTool);
            }
        }

        if (!sync.jumpDetach()) {
            player.setDeltaMovement(Vec3.ZERO);
        }
    }
}
