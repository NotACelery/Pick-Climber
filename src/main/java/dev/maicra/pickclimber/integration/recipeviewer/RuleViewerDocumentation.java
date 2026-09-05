package dev.maicra.pickclimber.integration.recipeviewer;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import dev.maicra.pickclimber.ModItems;
import dev.maicra.pickclimber.PickClimber;
import dev.maicra.pickclimber.rules.ClimbingRuleBookDefinition;
import dev.maicra.pickclimber.rules.ClimbingRulesProfile;
import dev.maicra.pickclimber.rules.item.ClimbingRuleBookData;

public final class RuleViewerDocumentation {
    public static final ResourceLocation CATEGORY_ID = ResourceLocation.fromNamespaceAndPath(
            PickClimber.MOD_ID,
            "rules_documentation"
    );
    public static final String CATEGORY_TRANSLATION_KEY = "emi.category.pickclimber.rules_documentation";

    private RuleViewerDocumentation() {
    }

    public static ClimbingRuleBookDefinition baseRuleBookDefinition() {
        String name = Component.translatable("gui.pickclimber.rules.new_profile").getString();
        return ClimbingRuleBookDefinition.permanentWorld(
                name,
                ClimbingRulesProfile.defaults(name)
        );
    }

    public static ItemStack baseRuleBook() {
        return ClimbingRuleBookData.create(baseRuleBookDefinition());
    }

    public static ItemStack vanillaBook() {
        return Items.BOOK.getDefaultInstance();
    }

    public static ItemStack rulesTable() {
        return ModItems.CLIMBING_RULES_TABLE.get().getDefaultInstance();
    }

    public static ItemStack rulesTerminal() {
        return ModItems.CLIMBING_RULES_TERMINAL.get().getDefaultInstance();
    }
}
