package dev.maicra.pickclimber.rules.block;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.Nullable;

import dev.maicra.pickclimber.rules.MapmakerPermissions;
import dev.maicra.pickclimber.rules.network.RulesEditorSessionStore;

public final class ClimbingRulesTableBlock extends BaseEntityBlock {
    public static final MapCodec<ClimbingRulesTableBlock> CODEC = simpleCodec(ClimbingRulesTableBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty HAS_BOOK = BooleanProperty.create("has_book");

    private static final VoxelShape NORTH_SHAPE = buildNorthShape();
    private static final VoxelShape EAST_SHAPE = rotateShape(NORTH_SHAPE);
    private static final VoxelShape SOUTH_SHAPE = rotateShape(EAST_SHAPE);
    private static final VoxelShape WEST_SHAPE = rotateShape(SOUTH_SHAPE);

    public ClimbingRulesTableBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(
                stateDefinition.any()
                        .setValue(FACING, Direction.NORTH)
                        .setValue(HAS_BOOK, false)
        );
    }

    @Override
    public MapCodec<ClimbingRulesTableBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos position, BlockState state) {
        return new ClimbingRulesTableBlockEntity(position, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(HAS_BOOK, false);
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
        builder.add(FACING, HAS_BOOK);
    }

    @Override
    protected VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos position,
            CollisionContext context
    ) {
        return shapeForFacing(state.getValue(FACING));
    }

    @Override
    protected VoxelShape getCollisionShape(
            BlockState state,
            BlockGetter level,
            BlockPos position,
            CollisionContext context
    ) {
        return shapeForFacing(state.getValue(FACING));
    }

    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos position) {
        return 1.0F;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos position) {
        return true;
    }

    private static VoxelShape shapeForFacing(Direction facing) {
        return switch (facing) {
            case NORTH -> NORTH_SHAPE;
            case EAST -> EAST_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            default -> NORTH_SHAPE;
        };
    }

    private static VoxelShape buildNorthShape() {
        VoxelShape shape = Shapes.empty();

        // Four legs: exactly the 2x6x2 supports authored in the final Blockbench model.
        shape = join(shape, Block.box(0, 0, 0, 2, 6, 2));
        shape = join(shape, Block.box(14, 0, 0, 16, 6, 2));
        shape = join(shape, Block.box(0, 0, 14, 2, 6, 16));
        shape = join(shape, Block.box(14, 0, 14, 16, 6, 16));

        // Main shelf/body and the two side walls.
        shape = join(shape, Block.box(0, 6, 0, 16, 8, 16));
        shape = join(shape, Block.box(0, 8, 0, 1, 14, 16));
        shape = join(shape, Block.box(15, 8, 0, 16, 14, 16));
        shape = join(shape, Block.box(1, 8, 1, 15, 14, 3));
        shape = join(shape, Block.box(1, 8, 13, 15, 14, 15));

        // Writing surface and both raised rails.
        shape = join(shape, Block.box(0, 14, 1, 16, 15, 15));
        shape = join(shape, Block.box(0, 14, 0, 16, 16, 1));
        shape = join(shape, Block.box(0, 14, 15, 16, 16, 16));
        return shape;
    }

    private static VoxelShape join(VoxelShape first, VoxelShape second) {
        return Shapes.joinUnoptimized(first, second, BooleanOp.OR);
    }

    private static VoxelShape rotateShape(VoxelShape shape) {
        VoxelShape[] rotated = {Shapes.empty()};
        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
                rotated[0] = Shapes.or(
                        rotated[0],
                        Shapes.box(1.0D - maxZ, minY, minX, 1.0D - minZ, maxY, maxX)
                )
        );
        return rotated[0];
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
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.CONSUME;
        }
        if (!MapmakerPermissions.canManage(serverPlayer)) {
            serverPlayer.displayClientMessage(
                    Component.translatable("message.pickclimber.rules.permission_denied"),
                    true
            );
            return InteractionResult.CONSUME;
        }
        if (level.getBlockEntity(position) instanceof ClimbingRulesTableBlockEntity blockEntity) {
            serverPlayer.openMenu(blockEntity, buffer -> buffer.writeBlockPos(position));
        }
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
                && level.getBlockEntity(position) instanceof ClimbingRulesTableBlockEntity blockEntity) {
            Containers.dropContents(level, position, blockEntity);
            if (!level.isClientSide()) {
                RulesEditorSessionStore.invalidateAt(level.dimension().location().toString(), position);
            }
        }
        super.onRemove(state, level, position, newState, movedByPiston);
    }
}
