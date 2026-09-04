package dev.maicra.pickclimber.client.rules;

import dev.maicra.pickclimber.ModItems;
import dev.maicra.pickclimber.rules.item.ClimbingRuleBookData;
import dev.maicra.pickclimber.rules.item.TemporaryRuleBookData;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

public final class RuleBookItemColors {
    private static final int OPAQUE_ALPHA = 0xFF000000;
    private static final int NO_TINT = 0xFFFFFFFF;

    private RuleBookItemColors() {
    }

    public static int color(ItemStack stack, int tintIndex) {
        if (tintIndex != 0) {
            return NO_TINT;
        }

        DyeColor coverColor;
        if (stack.is(ModItems.TEMPORARY_RULE_BOOK.get())) {
            coverColor = TemporaryRuleBookData.readDisplayMetadata(stack)
                    .map(TemporaryRuleBookData.DisplayMetadata::coverColor)
                    .orElse(DyeColor.WHITE);
        } else {
            coverColor = ClimbingRuleBookData.readReference(stack)
                    .map(ClimbingRuleBookData.Reference::coverColor)
                    .orElse(DyeColor.WHITE);
        }

        return OPAQUE_ALPHA | (coverColor.getTextureDiffuseColor() & 0x00FFFFFF);
    }
}
