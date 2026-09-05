package dev.maicra.pickclimber.integration.recipeviewer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import dev.maicra.pickclimber.PickClimber;

public enum RuleViewerRelation {
    AUTHORING("authoring"),
    APPLICATION("application");

    private final String path;

    RuleViewerRelation(String path) {
        this.path = path;
    }

    public ResourceLocation id() {
        return ResourceLocation.fromNamespaceAndPath(
                PickClimber.MOD_ID,
                "rules_documentation/" + path
        );
    }

    public ItemStack inputStack() {
        return switch (this) {
            case AUTHORING -> RuleViewerDocumentation.vanillaBook();
            case APPLICATION -> RuleViewerDocumentation.baseRuleBook();
        };
    }

    public ItemStack workstationStack() {
        return switch (this) {
            case AUTHORING -> RuleViewerDocumentation.rulesTable();
            case APPLICATION -> RuleViewerDocumentation.rulesTerminal();
        };
    }

    public ItemStack outputStack() {
        return this == AUTHORING
                ? RuleViewerDocumentation.baseRuleBook()
                : ItemStack.EMPTY;
    }

    public boolean hasOutput() {
        return this == AUTHORING;
    }
}
