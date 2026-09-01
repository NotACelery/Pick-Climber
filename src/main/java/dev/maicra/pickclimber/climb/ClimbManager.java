package dev.maicra.pickclimber.climb;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;

public final class ClimbManager {
    public static final int DURABILITY_COST = ClimbTuning.DURABILITY_COST;
    public static final int CRACK_STAGE = ClimbTuning.CRACK_STAGE;
    public static final int ANCHOR_COOLDOWN_TICKS = ClimbTuning.ANCHOR_COOLDOWN_TICKS;
    public static final int UNSTABLE_ANCHOR_COOLDOWN_TICKS = ClimbTuning.UNSTABLE_ANCHOR_COOLDOWN_TICKS;
    public static final double RISING_VELOCITY_THRESHOLD = ClimbTuning.RISING_VELOCITY_THRESHOLD;

    private ClimbManager() {
    }

    public static void recordRealJump(ServerPlayer player) {
        JumpTracker.record(player);
    }

    public static boolean isClimbingTool(ItemStack stack) {
        return ClimbingToolClassifier.isClimbingTool(stack);
    }

    public static boolean isAttached(Player player) {
        return player.level().isClientSide()
                ? ClimbStateStore.hasClient(player.getUUID())
                : ClimbStateStore.hasServer(player.getUUID());
    }

    public static InteractionHand activeHand(Player player) {
        InteractionHand activeHand = ClimbSessionView.activeHand(player);
        if (activeHand != null || !player.level().isClientSide()) {
            return activeHand;
        }
        RemoteAnchorPoseState remotePose = ClientClimbSynchronizer.remotePose(player);
        return remotePose == null ? null : remotePose.activeHand();
    }

    public static boolean isCeilingAnchor(Player player) {
        if (player.level().isClientSide()) {
            ClientClimbState state = ClimbStateStore.client(player.getUUID());
            return state != null && state.ceilingAnchor()
                    || ClientClimbSynchronizer.remotePose(player) != null;
        }

        ServerClimbState state = ClimbStateStore.server(player.getUUID());
        return state != null && state.anchorFace() == Direction.DOWN;
    }

    public static boolean isActiveTool(Player player, ItemStack stack) {
        return ClimbSessionView.isActiveTool(player, stack);
    }

    public static float visualCooldownFraction(Player player, ItemStack stack) {
        return AnchorCooldownService.fraction(stack, player.level().getGameTime());
    }

    public static float pinnedPoseProgress(Player player, ItemStack stack, float partialTick) {
        float blend = pinnedPoseBlend(player, stack, partialTick, false);
        return blend < 0.0F ? -1.0F : ClimbTuning.PINNED_SWING_PROGRESS * blend;
    }

    public static float ceilingPoseProgress(Player player, ItemStack stack, float partialTick) {
        return pinnedPoseBlend(player, stack, partialTick, true);
    }

    public static boolean isAttachmentCoherent(Player player) {
        return AnchorLifecycle.isAttachmentCoherent(player);
    }

    public static void recoverStaleAttachment(Player player) {
        AnchorLifecycle.recoverStaleAttachment(player);
    }

    public static boolean canAttemptAnchor(Player player, InteractionHand hand, BlockHitResult hit) {
        return AnchorEvaluator.evaluate(player, hit).forHand(hand).canAnchor();
    }

    public static AnchorIndicatorStatus anchorIndicatorStatus(Player player, BlockHitResult hit) {
        AnchorEvaluation evaluation = AnchorEvaluator.evaluate(player, hit);
        return AnchorFeedbackResolver.indicatorStatus(player, hit, evaluation);
    }

    public static Component anchorAttemptFailureMessage(Player player, BlockHitResult hit) {
        AnchorEvaluation evaluation = AnchorEvaluator.evaluate(player, hit);
        return AnchorFeedbackResolver.failureMessage(player, hit, evaluation);
    }

    public static boolean useClimbingTool(ServerPlayer player, InteractionHand hand, BlockHitResult hit) {
        return ClimbActionService.useClimbingTool(player, hand, hit);
    }

    public static boolean attach(ServerPlayer player, InteractionHand hand, BlockHitResult hit) {
        return ClimbActionService.attach(player, hand, hit);
    }

    public static void tick(Player player) {
        ClimbRuntimeTicker.tick(player);
    }

    public static void detachServer(ServerPlayer player, boolean jump) {
        AnchorLifecycle.detachServer(player, jump);
    }

    public static void updateSlideInput(
            ServerPlayer player,
            float forward,
            float strafe,
            float yaw,
            float pitch
    ) {
        AnchorInputStateService.update(player, forward, strafe, yaw, pitch);
    }

    public static void detachClient(Player player, boolean jump) {
        AnchorLifecycle.detachClient(player, jump);
    }

    public static void cleanupServer(ServerPlayer player) {
        AnchorLifecycle.cleanupServer(player);
    }

    public static void clearAllClientStates(Player localPlayer) {
        AnchorLifecycle.clearAllClientStates(localPlayer);
    }

    private static float pinnedPoseBlend(
            Player player,
            ItemStack stack,
            float partialTick,
            boolean requireCeilingAnchor
    ) {
        if (!player.level().isClientSide()) {
            return -1.0F;
        }

        ClientClimbState state = ClimbStateStore.client(player.getUUID());
        if (state == null
                || !ToolIdentity.matches(stack, state.toolId())
                || requireCeilingAnchor && !state.ceilingAnchor()) {
            return -1.0F;
        }

        float age = (float) (player.level().getGameTime() - state.poseStartedGameTime()) + partialTick;
        float blend = Mth.clamp(age / ClimbTuning.PINNED_POSE_RAMP_TICKS, 0.0F, 1.0F);
        return 1.0F - (1.0F - blend) * (1.0F - blend);
    }
}
