package dev.maicra.pickclimber.rules.block;

import dev.maicra.pickclimber.ModBlockEntities;
import dev.maicra.pickclimber.ModItems;
import dev.maicra.pickclimber.rules.RuleBookActivationMode;
import dev.maicra.pickclimber.rules.item.ClimbingRuleBookData;
import dev.maicra.pickclimber.rules.menu.ClimbingRuleDispenserMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class ClimbingRuleDispenserBlockEntity extends BaseContainerBlockEntity implements ContainerData {
    public static final int MASTER_SLOT = 0;
    public static final int SLOT_COUNT = 1;
    public static final int MIN_LIFETIME_SECONDS = 1;
    public static final int MAX_LIFETIME_SECONDS = 60;
    public static final int DEFAULT_LIFETIME_SECONDS = 30;

    private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private int lifetimeSeconds = DEFAULT_LIFETIME_SECONDS;

    public ClimbingRuleDispenserBlockEntity(BlockPos position, BlockState state) {
        super(ModBlockEntities.CLIMBING_RULE_DISPENSER.get(), position, state);
    }

    public ItemStack getMaster() {
        return getItem(MASTER_SLOT);
    }

    public int getLifetimeSeconds() {
        return lifetimeSeconds;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
        lifetimeSeconds = tag.contains("LifetimeSeconds")
                ? clampLifetime(tag.getInt("LifetimeSeconds"))
                : DEFAULT_LIFETIME_SECONDS;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        tag.putInt("LifetimeSeconds", lifetimeSeconds);
    }

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
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
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == MASTER_SLOT
                && stack.is(ModItems.CLIMBING_RULE_BOOK.get())
                && ClimbingRuleBookData.readCurrentDefinitionValidated(stack)
                .map(definition -> definition.activationMode() == RuleBookActivationMode.TEMPORARY)
                .orElse(false);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.pickclimber.climbing_rule_dispenser");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        ContainerLevelAccess access = level == null
                ? ContainerLevelAccess.NULL
                : ContainerLevelAccess.create(level, worldPosition);
        return new ClimbingRuleDispenserMenu(id, inventory, this, access, this);
    }

    @Override
    public int get(int index) {
        return index == 0 ? lifetimeSeconds : 0;
    }

    @Override
    public void set(int index, int value) {
        if (index != 0) {
            return;
        }
        lifetimeSeconds = clampLifetime(value);
        setChanged();
    }

    @Override
    public int getCount() {
        return 1;
    }

    public static int clampLifetime(int value) {
        return Math.max(MIN_LIFETIME_SECONDS, Math.min(MAX_LIFETIME_SECONDS, value));
    }
}
