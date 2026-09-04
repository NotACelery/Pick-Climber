package dev.maicra.pickclimber.rules;

import dev.maicra.pickclimber.climb.AnchorSurface;
import dev.maicra.pickclimber.climb.AnchorSurfaceClassifier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedHashSet;
import java.util.Set;

public final class DefaultRuleProfileFactory {
    private DefaultRuleProfileFactory() {
    }

    public static ClimbingRulesProfile create(String profileName) {
        Set<ResourceLocation> stable = new LinkedHashSet<>();
        Set<ResourceLocation> unstable = new LinkedHashSet<>();
        Set<ResourceLocation> unclimbable = new LinkedHashSet<>();

        for (Block block : BuiltInRegistries.BLOCK) {
            if (!isAuthorable(block)) {
                continue;
            }
            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
            AnchorSurface baseline = AnchorSurfaceClassifier.classify(block.defaultBlockState());
            if (baseline == AnchorSurface.UNSTABLE) {
                unstable.add(id);
            } else if (baseline == AnchorSurface.UNCLIMBABLE) {
                unclimbable.add(id);
            } else {
                stable.add(id);
            }
        }

        return new ClimbingRulesProfile(
                ClimbingRulesProfile.CURRENT_FORMAT_VERSION,
                profileName,
                stable,
                unstable,
                unclimbable,
                UnlistedPolicy.USE_PICK_CLIMBER_DEFAULTS,
                ClimbingRulesProfile.DEFAULT_PICKAXE_WEAR,
                true,
                false
        );
    }

    private static boolean isAuthorable(Block block) {
        if (block == Blocks.AIR || block.asItem() == Items.AIR) {
            return false;
        }
        BlockState state = block.defaultBlockState();
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
        if (state.hasBlockEntity() || state.is(BlockTags.LEAVES) || id.getPath().contains("leaves")) {
            return false;
        }
        return state.isCollisionShapeFullBlock(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
    }
}
