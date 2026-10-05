package com.arthou.inventoryrollbackplus.gui;

import com.arthou.inventoryrollbackplus.data.PlayerSnapshot;
import com.arthou.inventoryrollbackplus.data.SnapshotType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class TransferConfirmMenu extends AbstractContainerMenu {
    private final ServerPlayer viewer;
    private final ServerPlayer sourceTarget;
    private final ServerPlayer destination;
    private final PlayerSnapshot snapshot;
    private final int snapshotIndex;
    private final boolean enderChestView;
    private final SnapshotType sourceType;
    private final int sourcePage;
    private final SnapshotViewMode mode;
    private final SimpleContainer container;

    public TransferConfirmMenu(int containerId, Inventory playerInventory, ServerPlayer viewer, ServerPlayer sourceTarget, ServerPlayer destination, PlayerSnapshot snapshot, int snapshotIndex, boolean enderChestView, SnapshotType sourceType, int sourcePage, SnapshotViewMode mode) {
        super(MenuType.GENERIC_9x3, containerId);
        this.viewer = viewer;
        this.sourceTarget = sourceTarget;
        this.destination = destination;
        this.snapshot = snapshot;
        this.snapshotIndex = snapshotIndex;
        this.enderChestView = enderChestView;
        this.sourceType = sourceType;
        this.sourcePage = sourcePage;
        this.mode = mode;
        this.container = new SimpleContainer(27);

        populate();

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int slotIndex = col + row * 9;
                addSlot(new LockedSlot(container, slotIndex, 8 + col * 18, 18 + row * 18));
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 86 + row * 18));
            }
        }

        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 144));
        }
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId == 11) {
            SnapshotTransferSupport.transferAll(sourceTarget, destination, snapshot);
            viewer.sendSystemMessage(Component.literal("Transferred snapshot from " + sourceTarget.getGameProfile().getName() + " to " + destination.getGameProfile().getName() + "."));
            viewer.closeContainer();
            return;
        }
        if (slotId == 15) {
            viewer.openMenu(new SimpleMenuProvider((containerId, playerInventory, ignored) ->
                new TransferTargetMenu(containerId, playerInventory, viewer, sourceTarget, snapshot, snapshotIndex, enderChestView, sourceType, sourcePage, mode),
                Component.literal("Transfer To Player")));
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    private void populate() {
        container.setItem(11, named(Items.LIME_WOOL, "Yes, Transfer Everything"));
        container.setItem(13, named(Items.CHEST, "Transfer to " + destination.getGameProfile().getName()));
        container.setItem(15, named(Items.RED_WOOL, "Cancel"));
    }

    private static ItemStack named(net.minecraft.world.item.Item item, String name) {
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(name));
        return stack;
    }

    private static final class LockedSlot extends Slot {
        private LockedSlot(SimpleContainer container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
}
