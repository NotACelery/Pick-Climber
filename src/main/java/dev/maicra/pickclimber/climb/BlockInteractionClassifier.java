package dev.maicra.pickclimber.climb;

import dev.maicra.pickclimber.PickClimber;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockInteractionClassifier {
    public static final TagKey<Block> INTERACTIVE_BLOCKS = TagKey.create(
            Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(PickClimber.MOD_ID, "interactive_blocks")
    );

    private BlockInteractionClassifier() {
    }

    public static boolean handlesNormalBlockUse(BlockState state, Level level, BlockPos pos) {
        return state.getMenuProvider(level, pos) != null
                || level.getBlockEntity(pos) != null
                || state.is(INTERACTIVE_BLOCKS);
    }
}
