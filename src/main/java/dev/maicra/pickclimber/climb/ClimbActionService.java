package dev.maicra.pickclimber.climb;

import com.mojang.logging.LogUtils;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import org.slf4j.Logger;

final class ClimbActionService {
    private static final Logger LOGGER = LogUtils.getLogger();

    private ClimbActionService() {
    }

    static boolean useClimbingTool(ServerPlayer player, InteractionHand hand, BlockHitResult hit) {
        AnchorEvaluation evaluation = AnchorEvaluator.evaluate(player, hit);
        if (!evaluation.forHand(hand).canAnchor()) {
            return false;
        }

        boolean activelyFlying = player.getAbilities().flying;
        boolean rising = player.getDeltaMovement().y > ClimbTuning.RISING_VELOCITY_THRESHOLD;
        boolean airborne = !player.onGround();
        boolean realJumpAuthorized = JumpTracker.hasFreshUnconsumed(player);
        boolean chooseBoost = !activelyFlying
                && !ClimbStateStore.hasServer(player.getUUID())
                && airborne
                && rising
                && realJumpAuthorized
                && hit.getDirection().getAxis() != Direction.Axis.Y;

        LOGGER.info(
                "[PickClimber] action={} player={} hand={} onGround={} flying={} deltaY={} "
                        + "jumpAuthorized={} attached={} target={}",
                chooseBoost ? "BOOST" : "ATTACH",
                player.getScoreboardName(),
                hand,
                player.onGround(),
                activelyFlying,
                player.getDeltaMovement().y,
                realJumpAuthorized,
                ClimbStateStore.hasServer(player.getUUID()),
                hit.getBlockPos()
        );

        if (chooseBoost) {
            boolean boosted = performClimbingBoost(player, hand, hit, evaluation);
            if (boosted) {
                JumpTracker.consume(player);
            }
            return boosted;
        }

        return AnchorLifecycle.attachEvaluated(player, hand, hit, evaluation);
    }

    static boolean attach(ServerPlayer player, InteractionHand hand, BlockHitResult hit) {
        AnchorEvaluation evaluation = AnchorEvaluator.evaluate(player, hit);
        return AnchorLifecycle.attachEvaluated(player, hand, hit, evaluation);
    }

    private static boolean performClimbingBoost(
            ServerPlayer player,
            InteractionHand hand,
            BlockHitResult hit,
            AnchorEvaluation evaluation
    ) {
        AnchorHandEvaluation handEvaluation = evaluation.forHand(hand);
        if (!handEvaluation.canAnchor()) {
            return false;
        }

        ItemStack stack = player.getItemInHand(hand);
        ServerLevel level = player.serverLevel();
        BlockState targetState = evaluation.blockState();
        Vec3 boostedVelocity = AnchorImpulseCalculator.climbingBoostVelocity(player, stack);
        ToolWearService.damageHand(player, hand, ToolWearReason.CLIMBING_BOOST);
        player.setDeltaMovement(boostedVelocity);
        player.fallDistance = 0.0F;
        player.setOnGround(false);

        if (!stack.isEmpty()) {
            AnchorCooldownService.start(player, stack);
        }
        AnchorVisualService.playAnchorSound(level, player, targetState, hit.getBlockPos());
        ClimbSynchronization.sendBoost(
                player,
                boostedVelocity,
                hand,
                ClimbTuning.ANCHOR_COOLDOWN_TICKS
        );
        return true;
    }
}
