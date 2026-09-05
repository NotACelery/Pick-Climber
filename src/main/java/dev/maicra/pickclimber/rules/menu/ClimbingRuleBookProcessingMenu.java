package dev.maicra.pickclimber.rules.menu;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import dev.maicra.pickclimber.ModBlocks;
import dev.maicra.pickclimber.ModItems;
import dev.maicra.pickclimber.ModMenus;
import dev.maicra.pickclimber.rules.ClimbingRuleBookDefinition;
import dev.maicra.pickclimber.rules.ClimbingRuleBookProcessing;
import dev.maicra.pickclimber.rules.block.ClimbingRulesTableBlockEntity;
import dev.maicra.pickclimber.rules.item.ClimbingRuleBookData;

public final class ClimbingRuleBookProcessingMenu extends AbstractContainerMenu {
    private static final int SOURCE_SLOT = 0;
    private static final int MATERIAL_SLOT = 1;
    private static final int RESULT_SLOT = 2;
    private static final int PLAYER_START = 3;

    private final SimpleContainer inputs;
    private final ResultContainer result = new ResultContainer();
    private final ContainerLevelAccess access;
    private final BlockPos blockPos;
    private final MinecraftServer server;

    public ClimbingRuleBookProcessingMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, new SimpleContainer(2), ContainerLevelAccess.NULL, buffer.readBlockPos());
    }

    public ClimbingRuleBookProcessingMenu(
            int id,
            Inventory inventory,
            ClimbingRulesTableBlockEntity table,
            ContainerLevelAccess access
    ) {
        this(id, inventory, createInputs(table), access, table.getBlockPos());
    }

    private ClimbingRuleBookProcessingMenu(
            int id,
            Inventory inventory,
            SimpleContainer inputs,
            ContainerLevelAccess access,
            BlockPos blockPos
    ) {
        super(ModMenus.CLIMBING_RULE_BOOK_PROCESSING.get(), id);
        this.inputs = inputs;
        this.access = access;
        this.blockPos = blockPos;
        this.server = inventory.player instanceof ServerPlayer serverPlayer
                ? serverPlayer.serverLevel().getServer()
                : null;
        addInputSlots();
        addResultSlot();
        addPlayerInventory(inventory);
        slotsChanged(inputs);
    }

    public BlockPos blockPos() {
        return blockPos;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.CLIMBING_RULES_TABLE.get());
    }

    @Override
    public void slotsChanged(Container changed) {
        super.slotsChanged(changed);
        updateResult();
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (player.level().isClientSide()) {
            return;
        }
        ItemStack source = inputs.removeItemNoUpdate(SOURCE_SLOT);
        if (!source.isEmpty()
                && player.level().getBlockEntity(blockPos) instanceof ClimbingRulesTableBlockEntity table
                && table.getItem(ClimbingRulesTableBlockEntity.WORK_SLOT).isEmpty()) {
            ItemStack one = source.split(1);
            table.setItem(ClimbingRulesTableBlockEntity.WORK_SLOT, one);
            table.setChanged();
        }
        returnToPlayer(player, source);
        returnToPlayer(player, inputs.removeItemNoUpdate(MATERIAL_SLOT));
        result.clearContent();
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        super.clicked(slotId, button, clickType, player);
        updateResult();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index == RESULT_SLOT) {
            return quickMoveResult(player);
        }
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index < PLAYER_START) {
            if (!moveItemStackTo(stack, PLAYER_START, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.is(ModItems.CLIMBING_RULE_BOOK.get())) {
            if (!moveItemStackTo(stack, SOURCE_SLOT, SOURCE_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.is(Items.BOOK) || stack.getItem() instanceof DyeItem) {
            if (!moveItemStackTo(stack, MATERIAL_SLOT, MATERIAL_SLOT + 1, false)) {
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

    private void addInputSlots() {
        addSlot(new Slot(inputs, SOURCE_SLOT, 46, 42) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModItems.CLIMBING_RULE_BOOK.get());
            }
        });
        addSlot(new Slot(inputs, MATERIAL_SLOT, 86, 42) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Items.BOOK) || stack.getItem() instanceof DyeItem;
            }
        });
    }

    private void addResultSlot() {
        addSlot(new Slot(result, 0, 142, 42) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                consume(1);
                super.onTake(player, stack);
            }
        });
    }

    private void addPlayerInventory(Inventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 30 + column * 18, 110 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 30 + column * 18, 168));
        }
    }

    private void updateResult() {
        Optional<ClimbingRuleBookDefinition> source = server == null
                ? Optional.empty()
                : ClimbingRuleBookData.resolveDefinition(server, inputs.getItem(SOURCE_SLOT));
        ItemStack material = inputs.getItem(MATERIAL_SLOT);
        if (source.isEmpty() || material.isEmpty()) {
            result.setItem(0, ItemStack.EMPTY);
            broadcastChanges();
            return;
        }

        ClimbingRuleBookDefinition outputDefinition = source.get();
        if (material.getItem() instanceof DyeItem dyeItem) {
            if (dyeItem.getDyeColor() == source.get().coverColor()) {
                result.setItem(0, ItemStack.EMPTY);
                broadcastChanges();
                return;
            }
            outputDefinition = ClimbingRuleBookProcessing.recolor(source.get(), dyeItem.getDyeColor());
        } else if (!material.is(Items.BOOK)) {
            result.setItem(0, ItemStack.EMPTY);
            broadcastChanges();
            return;
        }
        result.setItem(0, ClimbingRuleBookData.create(server, outputDefinition));
        result.setChanged();
        broadcastChanges();
    }

    private ItemStack quickMoveResult(Player player) {
        ItemStack preview = result.getItem(0);
        if (preview.isEmpty()) {
            return ItemStack.EMPTY;
        }
        int available = maxProcessable();
        if (available <= 0) {
            return ItemStack.EMPTY;
        }
        ItemStack bulk = preview.copy();
        bulk.setCount(Math.min(available, bulk.getMaxStackSize()));
        int before = bulk.getCount();
        if (!moveItemStackTo(bulk, PLAYER_START, slots.size(), true)) {
            return ItemStack.EMPTY;
        }
        int moved = before - bulk.getCount();
        if (moved <= 0) {
            return ItemStack.EMPTY;
        }
        consume(moved);
        ItemStack movedStack = preview.copy();
        movedStack.setCount(moved);
        return movedStack;
    }

    private int maxProcessable() {
        ItemStack source = inputs.getItem(SOURCE_SLOT);
        ItemStack material = inputs.getItem(MATERIAL_SLOT);
        if (source.isEmpty() || material.isEmpty()) {
            return 0;
        }
        if (material.is(Items.BOOK)) {
            return material.getCount();
        }
        if (material.getItem() instanceof DyeItem) {
            return Math.min(source.getCount(), material.getCount());
        }
        return 0;
    }

    private void consume(int count) {
        ItemStack material = inputs.getItem(MATERIAL_SLOT);
        if (material.is(Items.BOOK)) {
            material.shrink(count);
        } else if (material.getItem() instanceof DyeItem) {
            inputs.getItem(SOURCE_SLOT).shrink(count);
            material.shrink(count);
        }
        inputs.setChanged();
        updateResult();
    }

    private static SimpleContainer createInputs(ClimbingRulesTableBlockEntity table) {
        SimpleContainer inputs = new SimpleContainer(2);
        ItemStack source = table.removeItemNoUpdate(ClimbingRulesTableBlockEntity.WORK_SLOT);
        if (!source.isEmpty()) {
            inputs.setItem(SOURCE_SLOT, source);
            table.setChanged();
        }
        return inputs;
    }

    private static void returnToPlayer(Player player, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }
}
