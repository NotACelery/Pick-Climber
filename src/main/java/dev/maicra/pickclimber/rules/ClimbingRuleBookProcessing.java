package dev.maicra.pickclimber.rules;

import net.minecraft.world.item.DyeColor;

public final class ClimbingRuleBookProcessing {
    private ClimbingRuleBookProcessing() {
    }

    public static ClimbingRuleBookDefinition recolor(
            ClimbingRuleBookDefinition definition,
            DyeColor color
    ) {
        if (definition.coverColor() == color) {
            return definition;
        }
        return new ClimbingRuleBookDefinition(
                definition.formatVersion(),
                definition.bookName(),
                color,
                definition.profile(),
                definition.activationMode(),
                definition.scope(),
                definition.durationSeconds(),
                definition.authorUuid(),
                definition.authorName()
        );
    }
}
