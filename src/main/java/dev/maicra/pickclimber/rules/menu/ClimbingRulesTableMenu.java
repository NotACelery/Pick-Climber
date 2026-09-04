package dev.maicra.pickclimber.rules.menu;

import dev.maicra.pickclimber.ModBlocks;
import dev.maicra.pickclimber.ModItems;
import dev.maicra.pickclimber.ModMenus;
import dev.maicra.pickclimber.rules.block.ClimbingRulesTableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class ClimbingRulesTableMenu extends AbstractContainerMenu {
    private final Container container;
    private final ContainerLevelAccess access;
    private final BlockPos blockPos;

    public ClimbingRulesTableMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(
                containerId,
                inventory,
                new SimpleContainer(ClimbingRulesTableBlockEntity.SLOT_COUNT),
                ContainerLevelAccess.NULL,
                buffer.readBlockPos()
        );
    }

    public ClimbingRulesTableMenu(
            int containerId,
            Inventory inventory,
            Container container,
            ContainerLevelAccess access
    ) {
        this(
                containerId,
                inventory,
                container,
                access,
                container instanceof ClimbingRulesTableBlockEntity blockEntity
                        ? blockEntity.getBlockPos()
                        : BlockPos.ZERO
        );
    }

    private ClimbingRulesTableMenu(
            int containerId,
            Inventory inventory,
            Container container,
            ContainerLevelAccess access,
            BlockPos blockPos
    ) {
        super(ModMenus.CLIMBING_RULES_TABLE.get(), containerId);
        this.container = container;
        this.access = access;
        this.blockPos = blockPos;
        checkContainerSize(container, ClimbingRulesTableBlockEntity.SLOT_COUNT);
        container.startOpen(inventory.player);

        addSlot(new Slot(container, ClimbingRulesTableBlockEntity.WORK_SLOT, 122, 42) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Items.BOOK) || stack.is(ModItems.CLIMBING_RULE_BOOK.get());
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        addPlayerInventory(inventory);
    }

    public BlockPos blockPos() {
        return blockPos;
    }

    public ItemStack workItem() {
        return container.getItem(ClimbingRulesTableBlockEntity.WORK_SLOT);
    }

    public ItemStack insertedItem() {
        return workItem();
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.CLIMBING_RULES_TABLE.get());
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
            if (!moveItemStackTo(stack, 1, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.is(Items.BOOK) || stack.is(ModItems.CLIMBING_RULE_BOOK.get())) {
            if (!moveItemStackTo(stack, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return copy;
    }

    private void addPlayerInventory(Inventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 49 + column * 18, 159 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 49 + column * 18, 217));
        }
    }
}
