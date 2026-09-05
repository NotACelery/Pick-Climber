package dev.maicra.pickclimber.rules.block;

import java.util.Optional;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

import dev.maicra.pickclimber.rules.ClimbingRuleBookDefinition;
import dev.maicra.pickclimber.rules.ClimbingRulesService;
import dev.maicra.pickclimber.rules.RuleBookApplicationResult;
import dev.maicra.pickclimber.rules.TemporaryRuleBookIssuanceService;
import dev.maicra.pickclimber.rules.item.ClimbingRuleBookData;
import dev.maicra.pickclimber.rules.item.TemporaryRuleBookData;

public final class ClimbingRulesTerminalBlock extends Block {
    public static final MapCodec<ClimbingRulesTerminalBlock> CODEC = simpleCodec(ClimbingRulesTerminalBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public ClimbingRulesTerminalBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP));
    }

    @Override
    public MapCodec<ClimbingRulesTerminalBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
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
    protected ItemInteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos position,
            Player player,
            InteractionHand hand,
            BlockHitResult hit
    ) {
        Optional<TemporaryRuleBookData.TransportData> transport = TemporaryRuleBookData.readValidated(stack);
        boolean regularRuleBook = ClimbingRuleBookData.hasCurrentSchema(stack);
        if (transport.isEmpty() && !regularRuleBook) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (level.isClientSide()) {
            return ItemInteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return ItemInteractionResult.FAIL;
        }
        Optional<ClimbingRuleBookDefinition> definition = transport
                .flatMap(data -> TemporaryRuleBookData.resolveDefinition(
                        serverPlayer.serverLevel().getServer(),
                        data
                ))
                .or(() -> ClimbingRuleBookData.resolveDefinition(serverPlayer.serverLevel().getServer(), stack));
        if (definition.isEmpty()) {
            serverPlayer.displayClientMessage(
                    Component.translatable("message.pickclimber.rules.valid_book_required"), true
            );
            return ItemInteractionResult.FAIL;
        }
        if (transport.isPresent()) {
            TemporaryRuleBookData.TransportData data = transport.get();
            long gameTime = serverPlayer.serverLevel().getServer().overworld().getGameTime();
            if (!data.owner().equals(serverPlayer.getUUID())
                    || data.expiresAtGameTime() <= gameTime
                    || !TemporaryRuleBookIssuanceService.isActive(data.owner(), data.issuanceToken(), gameTime)) {
                serverPlayer.displayClientMessage(
                        Component.translatable("message.pickclimber.rules.temporary_book_invalid_owner"),
                        true
                );
                return ItemInteractionResult.FAIL;
            }
        }

        RuleBookApplicationResult result = ClimbingRulesService.applyRuleBook(serverPlayer, definition.get());
        serverPlayer.displayClientMessage(Component.translatable(result.messageKey()), true);
        if (!result.success()) {
            return ItemInteractionResult.FAIL;
        }
        if (transport.isPresent()) {
            TemporaryRuleBookIssuanceService.release(
                    transport.get().issuanceToken(),
                    true,
                    serverPlayer.serverLevel().getServer()
            );
        }
        stack.shrink(1);
        return ItemInteractionResult.SUCCESS;
    }
}
