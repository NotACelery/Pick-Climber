package dev.maicra.pickclimber;

import dev.maicra.pickclimber.rules.menu.ClimbingRuleBookProcessingMenu;
import dev.maicra.pickclimber.rules.menu.ClimbingRulesTableMenu;
import dev.maicra.pickclimber.rules.menu.ClimbingRuleDispenserMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(
            Registries.MENU,
            PickClimber.MOD_ID
    );

    public static final DeferredHolder<MenuType<?>, MenuType<ClimbingRulesTableMenu>> CLIMBING_RULES_TABLE =
            MENUS.register(
                    "climbing_rules_table",
                    () -> IMenuTypeExtension.create(ClimbingRulesTableMenu::new)
            );


    public static final DeferredHolder<MenuType<?>, MenuType<ClimbingRuleBookProcessingMenu>>
            CLIMBING_RULE_BOOK_PROCESSING = MENUS.register(
                    "climbing_rule_book_processing",
                    () -> IMenuTypeExtension.create(ClimbingRuleBookProcessingMenu::new)
            );

    public static final DeferredHolder<MenuType<?>, MenuType<ClimbingRuleDispenserMenu>> CLIMBING_RULE_DISPENSER =
            MENUS.register("climbing_rule_dispenser", () -> IMenuTypeExtension.create(ClimbingRuleDispenserMenu::new));

    private ModMenus() {
    }
}
