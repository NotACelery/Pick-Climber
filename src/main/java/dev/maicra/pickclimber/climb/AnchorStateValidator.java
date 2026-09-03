package dev.maicra.pickclimber.climb;

import com.mojang.logging.LogUtils;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

final class AnchorStateValidator {
    private static final Logger LOGGER = LogUtils.getLogger();

    private AnchorStateValidator() {
    }

    static ServerClimbState reconcileActiveHand(ServerPlayer player, ServerClimbState state) {
        ItemStack current = player.getItemInHand(state.activeHand());
        if (ClimbingToolClassifier.isClimbingTool(current)
                && ToolIdentity.matches(current, state.toolId())) {
            return state;
        }

        InteractionHand otherHand = state.activeHand() == InteractionHand.MAIN_HAND
                ? InteractionHand.OFF_HAND
                : InteractionHand.MAIN_HAND;
        ItemStack other = player.getItemInHand(otherHand);
        if (!ClimbingToolClassifier.isClimbingTool(other)
                || !ToolIdentity.matches(other, state.toolId())) {
            return state;
        }

        ServerClimbState transferred = state.withActiveHand(otherHand);
        ClimbStateStore.putServer(player.getUUID(), transferred);
        ClimbSynchronization.sendAttached(player, transferred, false);
        ClimbSynchronization.sendRemotePose(player, transferred);
        LOGGER.info(
                "[PickClimber] action=TRANSFER_HAND player={} from={} to={} target={}",
                player.getScoreboardName(),
                state.activeHand(),
                otherHand,
                state.anchorBlock()
        );
        return transferred;
    }

    static boolean isValid(ServerPlayer player, ServerClimbState state) {
        if (!player.isAlive() || player.isSpectator() || player.isFallFlying()) {
            return false;
        }
        if (!player.level().dimension().equals(state.anchorDimension())) {
            return false;
        }

        ItemStack held = player.getItemInHand(state.activeHand());
        if (!ClimbingToolClassifier.isClimbingTool(held)
                || !ToolIdentity.matches(held, state.toolId())) {
            return false;
        }
        if (player.position().distanceToSqr(state.targetPosition())
                > ClimbTuning.MAX_DRIFT_DISTANCE_SQR) {
            return false;
        }

        BlockState blockState = player.level().getBlockState(state.anchorBlock());
        if (AnchorSurfaceResolver.resolve(player, state.anchorBlock(), blockState) == AnchorSurface.UNCLIMBABLE) {
            return false;
        }
        if (state.anchorFace() == Direction.DOWN) {
            return AnchorGeometry.hasValidCeilingAnchor(player, blockState, state.anchorBlock(), held);
        }
        return AnchorGeometry.hasValidAnchorFace(
                player,
                blockState,
                state.anchorBlock(),
                state.anchorFace()
        );
    }
}
