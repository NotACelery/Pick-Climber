package dev.maicra.pickclimber.climb;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import dev.maicra.pickclimber.PickClimber;

public final class AnchorSurfaceClassifier {
    public static final TagKey<Block> STABLE_ANCHOR_BLOCKS = tag("stable_anchor_blocks");
    public static final TagKey<Block> UNSTABLE_ANCHOR_BLOCKS = tag("unstable_anchor_blocks");
    public static final TagKey<Block> UNCLIMBABLE_BLOCKS = tag("unclimbable_blocks");

    private AnchorSurfaceClassifier() {
    }

    public static AnchorSurface classify(BlockState state) {
        String blockPath = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        if (state.is(BlockTags.LEAVES) || blockPath.contains("leaves")) {
            return AnchorSurface.UNCLIMBABLE;
        }
        if (state.is(UNCLIMBABLE_BLOCKS)) {
            return AnchorSurface.UNCLIMBABLE;
        }
        if (state.is(UNSTABLE_ANCHOR_BLOCKS)) {
            return AnchorSurface.UNSTABLE;
        }
        if (state.is(STABLE_ANCHOR_BLOCKS)) {
            return AnchorSurface.STABLE;
        }
        return AnchorSurface.FALLBACK;
    }

    private static TagKey<Block> tag(String path) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(PickClimber.MOD_ID, path));
    }
}
