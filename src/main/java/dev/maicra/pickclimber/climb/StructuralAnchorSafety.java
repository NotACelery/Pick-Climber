package dev.maicra.pickclimber.climb;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public final class StructuralAnchorSafety {
    public enum Classification {
        ANCHORABLE,
        STRUCTURALLY_NON_ANCHORABLE
    }

    private StructuralAnchorSafety() {
    }

    public static Classification classify(BlockGetter level, BlockPos position, BlockState state) {
        String blockPath = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        if (state.isAir()
                || state.is(BlockTags.LEAVES)
                || blockPath.contains("leaves")
                || !state.isCollisionShapeFullBlock(level, position)) {
            return Classification.STRUCTURALLY_NON_ANCHORABLE;
        }
        return Classification.ANCHORABLE;
    }

    public static boolean isStructurallyAnchorable(BlockGetter level, BlockPos position, BlockState state) {
        return classify(level, position, state) == Classification.ANCHORABLE;
    }
}
