package dev.maicra.pickclimber.climb;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.Optional;
import java.util.UUID;

/** Datos persistentes propios de cada pico usado por Pick Climber. */
public final class ToolIdentity {
    private static final String ROOT_KEY = "pickclimber";
    private static final String TOOL_ID_KEY = "anchor_tool_id";
    private static final String COOLDOWN_UNTIL_KEY = "cooldown_until";

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

    /**
     * Algunas actualizaciones de inventario pueden reconstruir temporalmente el
     * ItemStack y perder el custom data durante el mismo cambio de tick. Si el
     * objeto sigue siendo un pico en la mano correcta y no tiene ID, se repara
     * el ID esperado en vez de cancelar el anclaje inmediatamente.
     */
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
        return root.contains(COOLDOWN_UNTIL_KEY) ? root.getLong(COOLDOWN_UNTIL_KEY) : Long.MIN_VALUE;
    }

    public static boolean isCoolingDown(ItemStack stack, long gameTime) {
        return gameTime < cooldownUntil(stack);
    }

    public static float cooldownFraction(ItemStack stack, long gameTime, int duration) {
        long remaining = cooldownUntil(stack) - gameTime;
        if (remaining <= 0L || duration <= 0) {
            return 0.0F;
        }
        return Math.min(1.0F, remaining / (float) duration);
    }

    public static void startCooldown(ItemStack stack, long until) {
        if (stack.isEmpty()) {
            return;
        }

        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            CompoundTag root = tag.getCompound(ROOT_KEY);
            root.putLong(COOLDOWN_UNTIL_KEY, until);
            tag.put(ROOT_KEY, root);
        });
    }

    public static void clearCooldown(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }

        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            CompoundTag root = tag.getCompound(ROOT_KEY);
            root.remove(COOLDOWN_UNTIL_KEY);
            tag.put(ROOT_KEY, root);
        });
    }

    private static CompoundTag root(ItemStack stack) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return data.copyTag().getCompound(ROOT_KEY);
    }
}
