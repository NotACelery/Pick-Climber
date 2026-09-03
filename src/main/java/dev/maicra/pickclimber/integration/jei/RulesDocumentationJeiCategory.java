package dev.maicra.pickclimber.integration.jei;

import dev.maicra.pickclimber.PickClimber;
import dev.maicra.pickclimber.integration.recipeviewer.RuleViewerDocumentation;
import dev.maicra.pickclimber.integration.recipeviewer.RuleViewerRelation;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class RulesDocumentationJeiCategory implements IRecipeCategory<RuleViewerRelation> {
    public static final RecipeType<RuleViewerRelation> TYPE = RecipeType.create(
            PickClimber.MOD_ID,
            "rules_documentation",
            RuleViewerRelation.class
    );

    private static final int WIDTH = 116;
    private static final int HEIGHT = 38;

    private final IDrawable icon;
    private final IDrawableStatic arrow;
    private final IDrawableStatic plus;

    public RulesDocumentationJeiCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(RuleViewerDocumentation.baseRuleBook());
        this.arrow = guiHelper.getRecipeArrow();
        this.plus = guiHelper.getRecipePlusSign();
    }

    @Override
    public RecipeType<RuleViewerRelation> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable(RuleViewerDocumentation.CATEGORY_TRANSLATION_KEY);
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(
            IRecipeLayoutBuilder builder,
            RuleViewerRelation recipe,
            IFocusGroup focuses
    ) {
        if (recipe == RuleViewerRelation.AUTHORING) {
            addAuthoringSlots(builder, recipe);
            return;
        }
        addApplicationSlots(builder, recipe);
    }

    @Override
    public void draw(
            RuleViewerRelation recipe,
            IRecipeSlotsView recipeSlotsView,
            GuiGraphics guiGraphics,
            double mouseX,
            double mouseY
    ) {
        if (recipe == RuleViewerRelation.AUTHORING) {
            plus.draw(guiGraphics, 27, 12);
            arrow.draw(guiGraphics, 67, 10);
            return;
        }
        arrow.draw(guiGraphics, 48, 10);
    }

    @Override
    public ResourceLocation getRegistryName(RuleViewerRelation recipe) {
        return recipe.id();
    }

    private static void addAuthoringSlots(
            IRecipeLayoutBuilder builder,
            RuleViewerRelation recipe
    ) {
        builder.addInputSlot(5, 11)
                .setStandardSlotBackground()
                .addItemStack(recipe.inputStack());
        builder.addSlot(RecipeIngredientRole.CATALYST, 45, 11)
                .setStandardSlotBackground()
                .addItemStack(recipe.workstationStack());
        builder.addOutputSlot(95, 11)
                .setStandardSlotBackground()
                .addItemStack(recipe.outputStack());
    }

    private static void addApplicationSlots(
            IRecipeLayoutBuilder builder,
            RuleViewerRelation recipe
    ) {
        builder.addInputSlot(17, 11)
                .setStandardSlotBackground()
                .addItemStack(recipe.inputStack());
        builder.addSlot(RecipeIngredientRole.CATALYST, 84, 11)
                .setStandardSlotBackground()
                .addItemStack(recipe.workstationStack());
    }
}
