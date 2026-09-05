package dev.maicra.pickclimber.climb;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import dev.maicra.pickclimber.PickClimber;

public final class ClimbingToolClassifier {
    public static final TagKey<Item> CLIMBING_TOOLS = tag("climbing_tools");
    public static final TagKey<Item> EXCLUDED_CLIMBING_TOOLS = tag("excluded_climbing_tools");

    private ClimbingToolClassifier() {
    }

    public static boolean isClimbingTool(ItemStack stack) {
        return !stack.isEmpty()
                && !stack.is(EXCLUDED_CLIMBING_TOOLS)
                && stack.is(CLIMBING_TOOLS);
    }

    private static TagKey<Item> tag(String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(PickClimber.MOD_ID, path));
    }
}
