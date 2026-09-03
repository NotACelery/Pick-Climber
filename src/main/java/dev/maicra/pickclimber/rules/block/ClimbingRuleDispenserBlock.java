package dev.maicra.pickclimber.rules.block;

import com.mojang.serialization.MapCodec;
import dev.maicra.pickclimber.rules.MapmakerPermissions;
import dev.maicra.pickclimber.rules.RuleBookIssuanceResult;
import dev.maicra.pickclimber.rules.TemporaryRuleBookIssuanceService;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public final class ClimbingRuleDispenserBlock extends BaseEntityBlock {
    public static final MapCodec<ClimbingRuleDispenserBlock> CODEC = simpleCodec(ClimbingRuleDispenserBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public ClimbingRuleDispenserBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP));
    }

    @Override
    public MapCodec<ClimbingRuleDispenserBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos position, BlockState state) {
        return new ClimbingRuleDispenserBlockEntity(position, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos position,
            Player player,
            BlockHitResult hit
    ) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer serverPlayer)
                || !(level.getBlockEntity(position) instanceof ClimbingRuleDispenserBlockEntity blockEntity)) {
            return InteractionResult.CONSUME;
        }
        if (player.isSecondaryUseActive() && MapmakerPermissions.canManage(serverPlayer)) {
            serverPlayer.openMenu(blockEntity, buffer -> buffer.writeBlockPos(position));
            return InteractionResult.CONSUME;
        }

        RuleBookIssuanceResult result = TemporaryRuleBookIssuanceService.issue(serverPlayer, blockEntity);
        serverPlayer.displayClientMessage(Component.translatable(result.messageKey()), true);
        return InteractionResult.CONSUME;
    }

    @Override
    protected void onRemove(
            BlockState state,
            Level level,
            BlockPos position,
            BlockState newState,
            boolean movedByPiston
    ) {
        if (!state.is(newState.getBlock())
                && level.getBlockEntity(position) instanceof ClimbingRuleDispenserBlockEntity blockEntity) {
            Containers.dropContents(level, position, blockEntity);
        }
        super.onRemove(state, level, position, newState, movedByPiston);
    }
}
