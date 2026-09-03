package dev.maicra.pickclimber.climb;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

final class ToolWearState {
    private static final String ROOT_KEY = "pickclimber";
    private static final String MULTIPLIER_KEY = "wear_fraction_multiplier";
    private static final String REMAINDER_KEY = "wear_fraction_remainder";
    private static final String POLICY_REVISION_KEY = "wear_fraction_policy_revision";

    private ToolWearState() {
    }

    static int scaleDamage(
            ItemStack stack,
            int baseAmount,
            int multiplierPercent,
            long policyRevision
    ) {
        if (stack.isEmpty() || baseAmount <= 0) {
            return 0;
        }

        int safeMultiplier = Math.max(0, Math.min(500, multiplierPercent));
        if (safeMultiplier == 100) {
            clearFraction(stack);
            return baseAmount;
        }
        if (safeMultiplier == 0) {
            clearFraction(stack);
            return 0;
        }

        CompoundTag root = root(stack);
        int previousMultiplier = root.contains(MULTIPLIER_KEY)
                ? root.getInt(MULTIPLIER_KEY)
                : Integer.MIN_VALUE;
        long previousRevision = root.contains(POLICY_REVISION_KEY)
                ? root.getLong(POLICY_REVISION_KEY)
                : Long.MIN_VALUE;
        int previousRemainder = previousMultiplier == safeMultiplier
                && previousRevision == policyRevision
                && root.contains(REMAINDER_KEY)
                ? root.getInt(REMAINDER_KEY)
                : 0;
        ToolWearMath.Result result = ToolWearMath.scale(baseAmount, safeMultiplier, previousRemainder);
        persistFraction(stack, safeMultiplier, policyRevision, result.remainder());
        return result.damage();
    }

    private static void persistFraction(
            ItemStack stack,
            int multiplierPercent,
            long policyRevision,
            int remainder
    ) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            CompoundTag root = tag.getCompound(ROOT_KEY);
            if (remainder == 0) {
                root.remove(MULTIPLIER_KEY);
                root.remove(REMAINDER_KEY);
                root.remove(POLICY_REVISION_KEY);
            } else {
                root.putInt(MULTIPLIER_KEY, multiplierPercent);
                root.putInt(REMAINDER_KEY, remainder);
                root.putLong(POLICY_REVISION_KEY, policyRevision);
            }
            tag.put(ROOT_KEY, root);
        });
    }

    private static void clearFraction(ItemStack stack) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            CompoundTag root = tag.getCompound(ROOT_KEY);
            root.remove(MULTIPLIER_KEY);
            root.remove(REMAINDER_KEY);
            root.remove(POLICY_REVISION_KEY);
            tag.put(ROOT_KEY, root);
        });
    }

    private static CompoundTag root(ItemStack stack) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return data.copyTag().getCompound(ROOT_KEY);
    }
}
