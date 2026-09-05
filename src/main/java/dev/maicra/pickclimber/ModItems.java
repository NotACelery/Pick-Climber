package dev.maicra.pickclimber;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import dev.maicra.pickclimber.rules.item.ClimbingRuleBookItem;
import dev.maicra.pickclimber.rules.item.TemporaryRuleBookItem;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(PickClimber.MOD_ID);

    public static final DeferredItem<Item> CLIMBING_PICK_ICON = ITEMS.registerSimpleItem("climbing_pick_icon");
    public static final DeferredItem<ClimbingRuleBookItem> CLIMBING_RULE_BOOK = ITEMS.registerItem(
            "climbing_rule_book",
            ClimbingRuleBookItem::new,
            new Item.Properties().stacksTo(64)
    );
    public static final DeferredItem<BlockItem> CLIMBING_RULES_TABLE = ITEMS.registerSimpleBlockItem(
            ModBlocks.CLIMBING_RULES_TABLE
    );
    public static final DeferredItem<BlockItem> CLIMBING_RULES_TERMINAL = ITEMS.registerSimpleBlockItem(
            ModBlocks.CLIMBING_RULES_TERMINAL
    );
    public static final DeferredItem<BlockItem> CLIMBING_RULE_DISPENSER = ITEMS.registerSimpleBlockItem(
            ModBlocks.CLIMBING_RULE_DISPENSER
    );
    public static final DeferredItem<TemporaryRuleBookItem> TEMPORARY_RULE_BOOK = ITEMS.registerItem(
            "temporary_rule_book",
            TemporaryRuleBookItem::new,
            new Item.Properties().stacksTo(1)
    );

    private ModItems() {
    }
}
