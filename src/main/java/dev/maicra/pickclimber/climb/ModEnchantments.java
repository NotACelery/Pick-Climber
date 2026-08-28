package dev.maicra.pickclimber.climb;

import dev.maicra.pickclimber.PickClimber;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;

public final class ModEnchantments {
    public static final ResourceKey<Enchantment> PICK_CLIMBER = ResourceKey.create(
            Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(PickClimber.MOD_ID, "pick_climber")
    );
    public static final ResourceKey<Enchantment> STURDY_LATCH = ResourceKey.create(
            Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(PickClimber.MOD_ID, "sturdy_latch")
    );
    public static final ResourceKey<Enchantment> STRONG_GRIP = ResourceKey.create(
            Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(PickClimber.MOD_ID, "strong_grip")
    );

    private ModEnchantments() {
    }

    public static int getPickClimberLevel(Level level, ItemStack stack) {
        return getLevel(level, stack, PICK_CLIMBER);
    }

    public static boolean hasSturdyLatch(Level level, ItemStack stack) {
        return getLevel(level, stack, STURDY_LATCH) > 0;
    }

    public static boolean hasStrongGrip(Level level, ItemStack stack) {
        return getLevel(level, stack, STRONG_GRIP) > 0;
    }

    private static int getLevel(Level level, ItemStack stack, ResourceKey<Enchantment> enchantment) {
        if (stack.isEmpty()) {
            return 0;
        }

        var enchantments = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        return stack.getEnchantmentLevel(enchantments.getOrThrow(enchantment));
    }
}
