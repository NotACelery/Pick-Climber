package dev.maicra.pickclimber.climb;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import org.junit.jupiter.api.Test;

import static dev.maicra.pickclimber.climb.StructuralAnchorSafety.Classification.ANCHORABLE;
import static dev.maicra.pickclimber.climb.StructuralAnchorSafety.Classification.STRUCTURALLY_NON_ANCHORABLE;
import static org.junit.jupiter.api.Assertions.assertEquals;

class StructuralAnchorSafetyTest {
    private static final BlockPos POSITION = BlockPos.ZERO;

    @Test
    void fullCollisionBlocksRemainStructurallyAnchorable() {
        assertClassification(ANCHORABLE, Blocks.STONE.defaultBlockState());
        assertClassification(ANCHORABLE, Blocks.SAND.defaultBlockState());
    }

    @Test
    void representativePartialAndNoCollisionShapesAreRejected() {
        assertClassification(STRUCTURALLY_NON_ANCHORABLE, Blocks.OAK_SLAB.defaultBlockState());
        assertClassification(STRUCTURALLY_NON_ANCHORABLE, Blocks.OAK_STAIRS.defaultBlockState());
        assertClassification(STRUCTURALLY_NON_ANCHORABLE, Blocks.OAK_FENCE.defaultBlockState());
        assertClassification(STRUCTURALLY_NON_ANCHORABLE, Blocks.COBBLESTONE_WALL.defaultBlockState());
        assertClassification(STRUCTURALLY_NON_ANCHORABLE, Blocks.GLASS_PANE.defaultBlockState());
        assertClassification(STRUCTURALLY_NON_ANCHORABLE, Blocks.OAK_DOOR.defaultBlockState());
        assertClassification(STRUCTURALLY_NON_ANCHORABLE, Blocks.OAK_TRAPDOOR.defaultBlockState());
        assertClassification(STRUCTURALLY_NON_ANCHORABLE, Blocks.TORCH.defaultBlockState());
    }

    @Test
    void classificationFollowsTheConcreteStateInsteadOfOnlyTheBlockId() {
        BlockState singleSlab = Blocks.OAK_SLAB.defaultBlockState();
        BlockState doubleSlab = singleSlab.setValue(SlabBlock.TYPE, SlabType.DOUBLE);

        assertClassification(STRUCTURALLY_NON_ANCHORABLE, singleSlab);
        assertClassification(ANCHORABLE, doubleSlab);
    }

    private static void assertClassification(
            StructuralAnchorSafety.Classification expected,
            BlockState state
    ) {
        assertEquals(expected, StructuralAnchorSafety.classify(EmptyBlockGetter.INSTANCE, POSITION, state));
    }
}
