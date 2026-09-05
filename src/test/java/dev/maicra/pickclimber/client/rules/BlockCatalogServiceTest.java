package dev.maicra.pickclimber.client.rules;

import net.minecraft.world.level.block.Blocks;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlockCatalogServiceTest {
    @Test
    void onlyFullCollisionBlocksWithoutBlockEntitiesAreAuthorable() {
        assertTrue(BlockCatalogService.isAuthorable(Blocks.STONE));
        assertTrue(BlockCatalogService.isAuthorable(Blocks.GLASS));

        assertFalse(BlockCatalogService.isAuthorable(Blocks.OAK_SLAB));
        assertFalse(BlockCatalogService.isAuthorable(Blocks.OAK_STAIRS));
        assertFalse(BlockCatalogService.isAuthorable(Blocks.OAK_FENCE));
        assertFalse(BlockCatalogService.isAuthorable(Blocks.GLASS_PANE));
        assertFalse(BlockCatalogService.isAuthorable(Blocks.OAK_TRAPDOOR));
        assertFalse(BlockCatalogService.isAuthorable(Blocks.CHEST));
        assertFalse(BlockCatalogService.isAuthorable(Blocks.FURNACE));
    }
}
