package dev.maicra.pickclimber.client.rules;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

final class BlockCatalogService {
    private static List<Entry> cachedEntries;

    private BlockCatalogService() {
    }

    static List<Entry> entries() {
        if (cachedEntries == null) {
            cachedEntries = buildCatalog();
        }
        return cachedEntries;
    }

    private static List<Entry> buildCatalog() {
        List<Entry> entries = new ArrayList<>();
        for (Block block : BuiltInRegistries.BLOCK) {
            if (block == Blocks.AIR || block.asItem() == Items.AIR) {
                continue;
            }
            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
            entries.add(new Entry(id, block.asItem().getDefaultInstance()));
        }
        entries.sort(Comparator.comparing(entry -> entry.id().toString()));
        return List.copyOf(entries);
    }

    record Entry(ResourceLocation id, ItemStack stack) {
        boolean matches(String query) {
            return stack.getHoverName().getString().toLowerCase(Locale.ROOT).contains(query)
                    || id.toString().toLowerCase(Locale.ROOT).contains(query)
                    || id.getNamespace().toLowerCase(Locale.ROOT).contains(query)
                    || id.getPath().toLowerCase(Locale.ROOT).contains(query);
        }
    }
}
