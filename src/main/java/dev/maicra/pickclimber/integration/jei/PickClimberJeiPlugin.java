package dev.maicra.pickclimber.integration.jei;

import dev.maicra.pickclimber.ModItems;
import dev.maicra.pickclimber.PickClimber;
import dev.maicra.pickclimber.integration.recipeviewer.RuleViewerRelation;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

@JeiPlugin
public final class PickClimberJeiPlugin implements IModPlugin {
    private static final ResourceLocation PLUGIN_ID = ResourceLocation.fromNamespaceAndPath(
            PickClimber.MOD_ID,
            "jei_plugin"
    );

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new RulesDocumentationJeiCategory(registration.getJeiHelpers().getGuiHelper())
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(
                RulesDocumentationJeiCategory.TYPE,
                List.of(RuleViewerRelation.values())
        );
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(
                ModItems.CLIMBING_RULES_TABLE.get(),
                RulesDocumentationJeiCategory.TYPE
        );
        registration.addRecipeCatalyst(
                ModItems.CLIMBING_RULES_TERMINAL.get(),
                RulesDocumentationJeiCategory.TYPE
        );
    }
}
