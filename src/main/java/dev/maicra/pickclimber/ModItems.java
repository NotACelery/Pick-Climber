package dev.maicra.pickclimber;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(PickClimber.MOD_ID);
    public static final DeferredItem<Item> CLIMBING_PICK_ICON = ITEMS.registerSimpleItem("climbing_pick_icon");

    private ModItems() {
    }
}
