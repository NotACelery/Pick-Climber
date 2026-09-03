package dev.maicra.pickclimber.integration.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.maicra.pickclimber.integration.recipeviewer.RuleViewerRelation;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public final class RulesDocumentationEmiRecipe implements EmiRecipe {
    private static final int WIDTH = 124;
    private static final int HEIGHT = 38;

    private final EmiRecipeCategory category;
    private final RuleViewerRelation relation;
    private final List<EmiIngredient> inputs;
    private final List<EmiIngredient> catalysts;
    private final List<EmiStack> outputs;

    public RulesDocumentationEmiRecipe(
            EmiRecipeCategory category,
            RuleViewerRelation relation
    ) {
        this.category = category;
        this.relation = relation;
        this.inputs = List.of(EmiStack.of(relation.inputStack()));
        this.catalysts = List.of(EmiStack.of(relation.workstationStack()));
        this.outputs = relation.hasOutput()
                ? List.of(EmiStack.of(relation.outputStack()))
                : List.of();
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return category;
    }

    @Override
    public ResourceLocation getId() {
        return relation.id();
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return inputs;
    }

    @Override
    public List<EmiIngredient> getCatalysts() {
        return catalysts;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return outputs;
    }

    @Override
    public int getDisplayWidth() {
        return WIDTH;
    }

    @Override
    public int getDisplayHeight() {
        return HEIGHT;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        if (relation == RuleViewerRelation.AUTHORING) {
            addAuthoringWidgets(widgets);
            return;
        }
        addApplicationWidgets(widgets);
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    @Override
    public boolean hideCraftable() {
        return true;
    }

    private void addAuthoringWidgets(WidgetHolder widgets) {
        widgets.addSlot(inputs.getFirst(), 5, 10);
        widgets.addTexture(EmiTexture.PLUS, 29, 12);
        widgets.addSlot(catalysts.getFirst(), 46, 10);
        widgets.addTexture(EmiTexture.EMPTY_ARROW, 70, 10);
        widgets.addSlot(outputs.getFirst(), 101, 10);
    }

    private void addApplicationWidgets(WidgetHolder widgets) {
        widgets.addSlot(inputs.getFirst(), 17, 10);
        widgets.addTexture(EmiTexture.EMPTY_ARROW, 50, 10);
        widgets.addSlot(catalysts.getFirst(), 85, 10);
    }
}
