package dev.maicra.pickclimber.climb;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.Optional;
import java.util.UUID;

public final class ToolIdentity {
    private static final String ROOT_KEY = "pickclimber";
    private static final String TOOL_ID_KEY = "anchor_tool_id";
    private static final String COOLDOWN_UNTIL_KEY = "cooldown_until";
    private static final String COOLDOWN_DURATION_KEY = "cooldown_duration";
    private static final int DEFAULT_COOLDOWN_TICKS = ClimbTuning.ANCHOR_COOLDOWN_TICKS;

    private ToolIdentity() {
    }

    public static UUID ensure(ItemStack stack) {
        Optional<UUID> existing = get(stack);
        if (existing.isPresent()) {
            return existing.get();
        }

        UUID created = UUID.randomUUID();
        assign(stack, created);
        return created;
    }

    public static void assign(ItemStack stack, UUID id) {
        if (stack.isEmpty()) {
            return;
        }

        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            CompoundTag root = tag.getCompound(ROOT_KEY);
            root.putUUID(TOOL_ID_KEY, id);
            tag.put(ROOT_KEY, root);
        });
    }

    public static Optional<UUID> get(ItemStack stack) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        CompoundTag root = root(stack);
        return root.hasUUID(TOOL_ID_KEY)
                ? Optional.of(root.getUUID(TOOL_ID_KEY))
                : Optional.empty();
    }

    public static boolean matches(ItemStack stack, UUID expectedId) {
        return get(stack).map(expectedId::equals).orElse(false);
    }

    public static boolean matchesOrRepair(ItemStack stack, UUID expectedId) {
        Optional<UUID> current = get(stack);
        if (current.isEmpty()) {
            assign(stack, expectedId);
            return true;
        }
        return expectedId.equals(current.get());
    }

    public static long cooldownUntil(ItemStack stack) {
        CompoundTag root = root(stack);

        return root.contains(COOLDOWN_UNTIL_KEY) ? root.getLong(COOLDOWN_UNTIL_KEY) : 0L;
    }

    public static boolean isCoolingDown(ItemStack stack, long gameTime) {
        long until = cooldownUntil(stack);
        return until > gameTime;
    }

    public static float cooldownFraction(ItemStack stack, long gameTime) {
        int duration = cooldownDuration(stack);
        if (duration <= 0) {
            return 0.0F;
        }

        long until = cooldownUntil(stack);
        if (until <= gameTime) {
            return 0.0F;
        }

        long remaining = until - gameTime;
        return Math.min(1.0F, remaining / (float) duration);
    }

    public static void startCooldown(ItemStack stack, long until) {
        startCooldown(stack, until, cooldownDuration(stack));
    }

    public static void startCooldown(ItemStack stack, long until, int duration) {
        if (stack.isEmpty()) {
            return;
        }

        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            CompoundTag root = tag.getCompound(ROOT_KEY);
            root.putLong(COOLDOWN_UNTIL_KEY, until);
            root.putInt(COOLDOWN_DURATION_KEY, Math.max(1, duration));
            tag.put(ROOT_KEY, root);
        });
    }

    public static int cooldownDuration(ItemStack stack) {
        CompoundTag root = root(stack);
        return root.contains(COOLDOWN_DURATION_KEY)
                ? Math.max(1, root.getInt(COOLDOWN_DURATION_KEY))
                : DEFAULT_COOLDOWN_TICKS;
    }

    public static void clearCooldown(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }

        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            CompoundTag root = tag.getCompound(ROOT_KEY);
            root.remove(COOLDOWN_UNTIL_KEY);
            root.remove(COOLDOWN_DURATION_KEY);
            tag.put(ROOT_KEY, root);
        });
    }

    private static CompoundTag root(ItemStack stack) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return data.copyTag().getCompound(ROOT_KEY);
    }
}
