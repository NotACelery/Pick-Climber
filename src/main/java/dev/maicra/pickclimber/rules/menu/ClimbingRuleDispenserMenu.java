package dev.maicra.pickclimber.rules.menu;

import dev.maicra.pickclimber.ModBlocks;
import dev.maicra.pickclimber.ModItems;
import dev.maicra.pickclimber.ModMenus;
import dev.maicra.pickclimber.rules.block.ClimbingRuleDispenserBlockEntity;
import dev.maicra.pickclimber.rules.item.ClimbingRuleBookData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class ClimbingRuleDispenserMenu extends AbstractContainerMenu {
    private static final int PLAYER_INVENTORY_START = 1;
    private static final int PLAYER_INVENTORY_END = 28;
    private static final int HOTBAR_START = 28;
    private static final int HOTBAR_END = 37;

    private final Container container;
    private final ContainerLevelAccess access;
    private final BlockPos blockPos;
    private final ContainerData data;

    public ClimbingRuleDispenserMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(
                id,
                inventory,
                new SimpleContainer(ClimbingRuleDispenserBlockEntity.SLOT_COUNT),
                ContainerLevelAccess.NULL,
                new SimpleContainerData(2),
                buffer.readBlockPos()
        );
    }

    public ClimbingRuleDispenserMenu(
            int id,
            Inventory inventory,
            Container container,
            ContainerLevelAccess access,
            ContainerData data
    ) {
        this(
                id,
                inventory,
                container,
                access,
                data,
                container instanceof ClimbingRuleDispenserBlockEntity blockEntity
                        ? blockEntity.getBlockPos()
                        : BlockPos.ZERO
        );
    }

    private ClimbingRuleDispenserMenu(
            int id,
            Inventory inventory,
            Container container,
            ContainerLevelAccess access,
            ContainerData data,
            BlockPos blockPos
    ) {
        super(ModMenus.CLIMBING_RULE_DISPENSER.get(), id);
        this.container = container;
        this.access = access;
        this.data = data;
        this.blockPos = blockPos;

        checkContainerSize(container, ClimbingRuleDispenserBlockEntity.SLOT_COUNT);
        addDataSlots(data);
        container.startOpen(inventory.player);
        addSourceSlot(container);
        addPlayerInventory(inventory);
    }

    public BlockPos blockPos() {
        return blockPos;
    }

    public int lifetimeSeconds() {
        return ClimbingRuleDispenserBlockEntity.clampLifetime(data.get(0));
    }

    public boolean startCounterOnPickup() {
        return data.get(1) != 0;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.CLIMBING_RULE_DISPENSER.get());
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        container.stopOpen(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index == 0) {
            if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (isRuleBookSource(stack)) {
            if (!moveItemStackTo(stack, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index < HOTBAR_START) {
            if (!moveItemStackTo(stack, HOTBAR_START, HOTBAR_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, PLAYER_INVENTORY_END, false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return copy;
    }

    private void addSourceSlot(Container container) {
        addSlot(new Slot(container, ClimbingRuleDispenserBlockEntity.SOURCE_SLOT, 27, 47) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return isRuleBookSource(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
    }

    private void addPlayerInventory(Inventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 36 + column * 18, 138 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 36 + column * 18, 196));
        }
    }

    private static boolean isRuleBookSource(ItemStack stack) {
        return stack.is(ModItems.CLIMBING_RULE_BOOK.get())
                && ClimbingRuleBookData.hasCurrentSchema(stack);
    }
}
