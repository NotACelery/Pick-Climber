package dev.maicra.pickclimber;

import dev.maicra.pickclimber.climb.ModEnchantments;
import dev.maicra.pickclimber.rules.ClimbingRulesProfile;
import dev.maicra.pickclimber.rules.item.ClimbingRuleBookData;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(
            Registries.CREATIVE_MODE_TAB,
            PickClimber.MOD_ID
    );

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> PICK_CLIMBER = TABS.register(
            "pick_climber",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.pickclimber"))
                    .icon(() -> new ItemStack(ModItems.CLIMBING_PICK_ICON.get()))
                    .displayItems((parameters, output) -> {
                        addEnchantedBook(parameters, output, ModEnchantments.PICK_CLIMBER, 3);
                        addEnchantedBook(parameters, output, ModEnchantments.STURDY_LATCH, 1);
                        addEnchantedBook(parameters, output, ModEnchantments.STRONG_GRIP, 1);
                        String defaultProfileName = Component.translatable(
                                "gui.pickclimber.rules.new_profile"
                        ).getString();
                        output.accept(ClimbingRuleBookData.create(
                                ClimbingRulesProfile.defaults(defaultProfileName)
                        ));
                        output.accept(ModItems.CLIMBING_RULES_TABLE.get());
                        output.accept(ModItems.CLIMBING_RULES_TERMINAL.get());
                        output.accept(ModItems.CLIMBING_RULE_DISPENSER.get());
                    })
                    .build()
    );

    private ModCreativeTabs() {
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
