package dev.maicra.pickclimber.integration.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.maicra.pickclimber.integration.recipeviewer.RuleViewerDocumentation;
import dev.maicra.pickclimber.integration.recipeviewer.RuleViewerRelation;

@EmiEntrypoint
public final class PickClimberEmiPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        EmiRecipeCategory category = new EmiRecipeCategory(
                RuleViewerDocumentation.CATEGORY_ID,
                EmiStack.of(RuleViewerDocumentation.baseRuleBook())
        );
        registry.addCategory(category);
        registry.addWorkstation(category, EmiStack.of(RuleViewerDocumentation.rulesTable()));
        registry.addWorkstation(category, EmiStack.of(RuleViewerDocumentation.rulesTerminal()));

        for (RuleViewerRelation relation : RuleViewerRelation.values()) {
            registry.addRecipe(new RulesDocumentationEmiRecipe(category, relation));
        }
    }
}
