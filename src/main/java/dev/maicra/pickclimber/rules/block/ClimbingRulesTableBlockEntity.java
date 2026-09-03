package dev.maicra.pickclimber.rules.block;

import dev.maicra.pickclimber.ModBlockEntities;
import dev.maicra.pickclimber.ModItems;
import dev.maicra.pickclimber.rules.menu.ClimbingRulesTableMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class ClimbingRulesTableBlockEntity extends BaseContainerBlockEntity {
    public static final int RULE_BOOK_SLOT = 0;
    public static final int MATERIAL_BOOK_SLOT = 1;
    public static final int DYE_SLOT = 2;
    public static final int SLOT_COUNT = 3;

    private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);

    public ClimbingRulesTableBlockEntity(BlockPos position, BlockState state) {
        super(ModBlockEntities.CLIMBING_RULES_TABLE.get(), position, state);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
    }

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return switch (slot) {
            case RULE_BOOK_SLOT -> stack.is(ModItems.CLIMBING_RULE_BOOK.get()) && stack.getCount() == 1;
            case MATERIAL_BOOK_SLOT -> stack.is(Items.BOOK);
            case DYE_SLOT -> stack.getItem() instanceof DyeItem;
            default -> false;
        };
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.pickclimber.climbing_rules_table");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new ClimbingRulesTableMenu(
                containerId,
                inventory,
                this,
                level == null ? ContainerLevelAccess.NULL : ContainerLevelAccess.create(level, worldPosition)
        );
    }
}
