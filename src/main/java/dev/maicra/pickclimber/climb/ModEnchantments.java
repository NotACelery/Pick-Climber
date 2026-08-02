package dev.maicra.pickclimber.climb;

import dev.maicra.pickclimber.PickClimber;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;

/** Claves y consultas de los encantamientos propios de Pick Climber. */
public final class ModEnchantments {
    public static final ResourceKey<Enchantment> PICK_CLIMBER = ResourceKey.create(
            Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(PickClimber.MOD_ID, "pick_climber")
    );

    private ModEnchantments() {
    }

    public static int getPickClimberLevel(Level level, ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }

        var enchantments = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        return stack.getEnchantmentLevel(enchantments.getOrThrow(PICK_CLIMBER));
    }
}
