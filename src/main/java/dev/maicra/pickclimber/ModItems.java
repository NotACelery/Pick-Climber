package dev.maicra.pickclimber;

import dev.maicra.pickclimber.climb.ModEnchantments;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(PickClimber.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(
            Registries.CREATIVE_MODE_TAB,
            PickClimber.MOD_ID
    );

    public static final DeferredItem<Item> CLIMBING_PICK_ICON = ITEMS.registerSimpleItem("climbing_pick_icon");

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> PICK_CLIMBER_TAB = CREATIVE_MODE_TABS.register(
            "pick_climber",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.pickclimber"))
                    .icon(() -> new ItemStack(CLIMBING_PICK_ICON.get()))
                    .displayItems((parameters, output) -> {
                        addEnchantedBook(parameters, output, ModEnchantments.PICK_CLIMBER, 3);
                        addEnchantedBook(parameters, output, ModEnchantments.STURDY_LATCH, 1);
                        addEnchantedBook(parameters, output, ModEnchantments.STRONG_GRIP, 1);
                    })
                    .build()
    );

    private ModItems() {
    }

    private static void addEnchantedBook(
            CreativeModeTab.ItemDisplayParameters parameters,
            CreativeModeTab.Output output,
            ResourceKey<Enchantment> key,
            int level
    ) {
        var enchantments = parameters.holders().lookupOrThrow(Registries.ENCHANTMENT);
        var enchantment = enchantments.getOrThrow(key);
        output.accept(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment, level)));
    }
}
