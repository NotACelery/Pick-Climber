package dev.maicra.pickclimber.climb;

import java.util.UUID;

import com.mojang.logging.LogUtils;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import org.slf4j.Logger;

final class AnchorLifecycle {
    private static final Logger LOGGER = LogUtils.getLogger();

    private AnchorLifecycle() {
    }

    static boolean isAttachmentCoherent(Player player) {
        if (player.level().isClientSide()) {
            ClientClimbState state = ClimbStateStore.client(player.getUUID());
            return state != null
                    && player.position().distanceToSqr(state.targetPosition())
                    <= ClimbTuning.ATTACHMENT_COHERENCE_DISTANCE_SQR
                    && player.isNoGravity();
        }

        ServerClimbState state = ClimbStateStore.server(player.getUUID());
        return state != null
                && player.position().distanceToSqr(state.targetPosition())
                <= ClimbTuning.ATTACHMENT_COHERENCE_DISTANCE_SQR
                && player.isNoGravity();
    }

    static void recoverStaleAttachment(Player player) {
        if (player.level().isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        ServerClimbState state = ClimbStateStore.server(player.getUUID());
        boolean refundCooldown = state != null
                && player.level().getGameTime() - state.attachedAtGameTime()
                <= ClimbTuning.FAILED_ATTACH_GRACE_TICKS;
        detachServer(serverPlayer, false, refundCooldown);
    }

    static boolean attachEvaluated(
            ServerPlayer player,
            InteractionHand hand,
            BlockHitResult hit,
            AnchorEvaluation evaluation
    ) {
        AnchorHandEvaluation handEvaluation = evaluation.forHand(hand);
        if (!handEvaluation.canAnchor()) {
            return false;
        }

        ServerClimbState previous = ClimbStateStore.server(player.getUUID());
        Vec3 target = handEvaluation.targetPosition();
        ServerLevel level = player.serverLevel();
        BlockState anchorState = evaluation.blockState();
        ItemStack stack = player.getItemInHand(hand);
        UUID toolId = ToolIdentity.ensure(stack);
        if (previous != null && previous.activeHand() != hand && previous.toolId().equals(toolId)) {
            toolId = UUID.randomUUID();
            ToolIdentity.assign(stack, toolId);
        }

        AnchorSurface surface = evaluation.surface();
        boolean reinforcedLatch = handEvaluation.sturdyLatch();
        boolean ceilingAnchor = evaluation.ceilingAttempt();
        AnchorMotion initialMotion = initialMotion(surface, player, reinforcedLatch, ceilingAnchor);
        ToolWearReason attachWear = ceilingAnchor
                ? ToolWearReason.CEILING_ATTACH
                : ToolWearReason.WALL_ATTACH;
        if (!ToolWearService.damageHand(player, hand, attachWear)) {
            return false;
        }

        UUID brakingSupportToolId = prepareBrakingSupport(player, hand, toolId, initialMotion);
        boolean restoreNoGravity = previous == null
                ? player.isNoGravity()
                : previous.restoreNoGravity();
        boolean restoreFlying = previous == null
                ? player.getAbilities().flying
                : previous.restoreFlying();
        int crackId = previous == null
                ? AnchorVisualService.createCrackId(player)
                : previous.crackId();

        if (previous != null) {
            AnchorVisualService.clearAnchorVisuals(player, previous);
        }

        ServerClimbState next = new ServerClimbState(
                level.dimension(),
                hit.getBlockPos().immutable(),
                hit.getDirection(),
                target,
                hand,
                toolId,
                crackId,
                restoreNoGravity,
                restoreFlying,
                player.level().getGameTime(),
                surface,
                initialMotion,
                initialSlideVelocity(surface, player, reinforcedLatch, ceilingAnchor),
                cooldownTicksFor(surface, reinforcedLatch),
                0.0F,
                0.0F,
                hit.getLocation().subtract(target).subtract(
                        hit.getDirection().getStepX() * ClimbTuning.CONTACT_BLOCK_EPSILON,
                        hit.getDirection().getStepY() * ClimbTuning.CONTACT_BLOCK_EPSILON,
                        hit.getDirection().getStepZ() * ClimbTuning.CONTACT_BLOCK_EPSILON
                ),
                Vec3.ZERO,
                reinforcedLatch,
                brakingSupportToolId,
                0.0D,
                0,
                target,
                Vec3.ZERO,
                Vec3.ZERO
        );

        ClimbStateStore.putServer(player.getUUID(), next);
        JumpTracker.clear(player);
        attachPlayerToTarget(player, target);

        AnchorVisualService.showCracks(level, next);
        AnchorVisualService.playAnchorSound(level, player, anchorState, next.anchorBlock());
        AnchorCooldownService.start(player, stack, next.cooldownTicks());
        if (initialMotion == AnchorMotion.BRAKING) {
            AnchorCooldownService.startOtherEquippedTools(player, hand);
        }
        ClimbSynchronization.sendAttached(player, next, true);
        ClimbSynchronization.sendRemotePose(player, next);
        return true;
    }

    static void detachServer(ServerPlayer player, boolean jump) {
        detachServer(player, jump, false);
    }

    static void detachServer(ServerPlayer player, boolean jump, boolean refundCooldown) {
        ServerClimbState state = ClimbStateStore.removeServer(player.getUUID());
        if (state == null) {
            return;
        }

        long attachmentAge = player.level().getGameTime() - state.attachedAtGameTime();
        LOGGER.info(
                "[PickClimber] action={} player={} hand={} attachmentAge={} target={}",
                jump ? "DETACH_JUMP" : "DETACH_PASSIVE",
                player.getScoreboardName(),
                state.activeHand(),
                attachmentAge,
                state.anchorBlock()
        );

        AnchorVisualService.clearAnchorVisuals(player, state);
        ItemStack activeTool = ToolLocator.findOwned(player, state.toolId());
        int remainingCooldownTicks = prepareDetachedCooldown(player, activeTool, refundCooldown);
        Vec3 detachVelocity = detachVelocity(player, state, activeTool, jump);

        restoreAbilities(player, state.restoreNoGravity(), state.restoreFlying());
        player.fallDistance = 0.0F;
        player.setDeltaMovement(detachVelocity);

        if (jump && state.anchorFace() != Direction.DOWN) {
            JumpTracker.markCurrentJumpUnconsumed(player);
        }

        ClimbSynchronization.sendDetached(
                player,
                state.restoreNoGravity(),
                state.restoreFlying(),
                jump,
                state.toolId(),
                remainingCooldownTicks,
                refundCooldown
        );
        ClimbSynchronization.sendRemotePoseDetached(player);
        if (jump && state.anchorFace() == Direction.DOWN) {
            ClimbSynchronization.sendBoost(player, detachVelocity, state.activeHand(), 0);
        }
    }

    static void detachClient(Player player, boolean jump) {
        ClientClimbState state = ClimbStateStore.removeClient(player.getUUID());
        if (state == null) {
            return;
        }

        AnchorVisualService.clearClientCracks(player, state);
        if (jump && !state.ceilingAnchor()) {
            ItemStack activeTool = player.getItemInHand(state.activeHand());
            player.setDeltaMovement(AnchorImpulseCalculator.wallJumpVelocity(player, activeTool));
        }
    }

    static void cleanupServer(ServerPlayer player) {
        JumpTracker.clear(player);
        ServerClimbState state = ClimbStateStore.removeServer(player.getUUID());
        if (state == null) {
            return;
        }

        AnchorVisualService.clearAnchorVisuals(player, state);
        restoreAbilities(player, state.restoreNoGravity(), state.restoreFlying());
        player.setDeltaMovement(Vec3.ZERO);
        ClimbSynchronization.sendRemotePoseDetached(player);
    }

    static void clearAllClientStates(Player localPlayer) {
        if (localPlayer != null) {
            ClientClimbState state = ClimbStateStore.removeClient(localPlayer.getUUID());
            if (state != null) {
                AnchorVisualService.clearClientCracks(localPlayer, state);
            }
        } else {
            ClimbStateStore.clearClients();
        }
        ClimbStateStore.clearRemotePoses();
    }

    private static UUID prepareBrakingSupport(
            ServerPlayer player,
            InteractionHand activeHand,
            UUID activeToolId,
            AnchorMotion initialMotion
    ) {
        if (initialMotion != AnchorMotion.BRAKING) {
            return null;
        }

        InteractionHand supportHand = activeHand == InteractionHand.MAIN_HAND
                ? InteractionHand.OFF_HAND
                : InteractionHand.MAIN_HAND;
        ItemStack supportStack = player.getItemInHand(supportHand);
        if (!ClimbingToolClassifier.isClimbingTool(supportStack)) {
            return null;
        }

        UUID supportToolId = ToolIdentity.ensure(supportStack);
        if (supportToolId.equals(activeToolId)) {
            supportToolId = UUID.randomUUID();
            ToolIdentity.assign(supportStack, supportToolId);
        }
        return ToolWearService.damageHand(player, supportHand, ToolWearReason.BRAKING_SUPPORT_ATTACH)
                ? supportToolId
                : null;
    }

    private static void attachPlayerToTarget(ServerPlayer player, Vec3 target) {
        player.setDeltaMovement(Vec3.ZERO);
        player.getAbilities().flying = false;
        player.onUpdateAbilities();
        player.setNoGravity(true);
        player.connection.teleport(
                target.x,
                target.y,
                target.z,
                player.getYRot(),
                player.getXRot()
        );
        player.setDeltaMovement(Vec3.ZERO);
        player.fallDistance = 0.0F;
        player.setOnGround(false);
    }

    private static int prepareDetachedCooldown(
            ServerPlayer player,
            ItemStack activeTool,
            boolean refundCooldown
    ) {
        if (activeTool.isEmpty()) {
            return 0;
        }
        if (refundCooldown) {
            AnchorCooldownService.clear(player, activeTool);
            return 0;
        }
        return AnchorCooldownService.remainingTicks(activeTool, player.level().getGameTime());
    }

    private static Vec3 detachVelocity(
            ServerPlayer player,
            ServerClimbState state,
            ItemStack activeTool,
            boolean jump
    ) {
        if (!jump) {
            return Vec3.ZERO;
        }
        return state.anchorFace() == Direction.DOWN
                ? AnchorImpulseCalculator.ceilingReleaseVelocity(player, state)
                : AnchorImpulseCalculator.wallJumpVelocity(player, activeTool);
    }

    private static void restoreAbilities(Player player, boolean noGravity, boolean flying) {
        player.setNoGravity(noGravity);
        player.getAbilities().flying = flying;
        player.onUpdateAbilities();
    }

    private static AnchorMotion initialMotion(
            AnchorSurface surface,
            ServerPlayer player,
            boolean reinforcedLatch,
            boolean ceilingAnchor
    ) {
        if (ceilingAnchor) {
            return AnchorMotion.FIXED;
        }
        if (player.getDeltaMovement().y < ClimbTuning.BRAKING_START_SPEED
                && player.fallDistance > ClimbTuning.BRAKING_MIN_FALL_DISTANCE) {
            return AnchorMotion.BRAKING;
        }
        return surface == AnchorSurface.UNSTABLE && !reinforcedLatch
                ? AnchorMotion.UNSTABLE_SLIDING
                : AnchorMotion.FIXED;
    }

    private static double initialSlideVelocity(
            AnchorSurface surface,
            ServerPlayer player,
            boolean reinforcedLatch,
            boolean ceilingAnchor
    ) {
        return initialMotion(surface, player, reinforcedLatch, ceilingAnchor) == AnchorMotion.BRAKING
                ? player.getDeltaMovement().y
                : surface == AnchorSurface.UNSTABLE ? ClimbTuning.UNSTABLE_SLIDE_SPEED : 0.0D;
    }

    private static int cooldownTicksFor(AnchorSurface surface, boolean reinforcedLatch) {
        return surface == AnchorSurface.UNSTABLE && !reinforcedLatch
                ? ClimbTuning.UNSTABLE_ANCHOR_COOLDOWN_TICKS
                : ClimbTuning.ANCHOR_COOLDOWN_TICKS;
    }
}
