package com.arthou.inventoryrollbackplus.gui;

import com.arthou.inventoryrollbackplus.data.InventoryRollbackSavedData;
import com.arthou.inventoryrollbackplus.data.PlayerSnapshot;
import com.arthou.inventoryrollbackplus.data.SnapshotType;
import com.arthou.inventoryrollbackplus.util.PlayerDataAccess;
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

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SnapshotEntriesMenu extends AbstractContainerMenu {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    private final ServerPlayer viewer;
    private final ServerPlayer target;
    private final SnapshotType snapshotType;
    private final int page;
    private final SnapshotViewMode mode;
    private final List<Integer> snapshotIndexes;
    private final SimpleContainer container;
    private final Map<Integer, Integer> slotToSnapshotIndex;

    public SnapshotEntriesMenu(int containerId, Inventory playerInventory, ServerPlayer viewer, ServerPlayer target, SnapshotType snapshotType, int page, SnapshotViewMode mode) {
        super(MenuType.GENERIC_9x6, containerId);
        this.viewer = viewer;
        this.target = target;
        this.snapshotType = snapshotType;
        this.page = page;
        this.mode = mode;
        this.container = new SimpleContainer(54);
        this.slotToSnapshotIndex = new HashMap<>();
        this.snapshotIndexes = collectIndexes(target, snapshotType);

        populate();

        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 9; col++) {
                int slotIndex = col + row * 9;
                addSlot(new LockedSlot(container, slotIndex, 8 + col * 18, 18 + row * 18));
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 140 + row * 18));
            }
        }

        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 198));
        }
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        Integer snapshotIndex = slotToSnapshotIndex.get(slotId);
        if (snapshotIndex != null) {
            viewer.openMenu(new SimpleMenuProvider((containerId, playerInventory, ignored) ->
                new SnapshotDetailMenu(containerId, playerInventory, viewer, target, snapshotIndex, false, snapshotType, page, mode),
                Component.literal(mode.title() + " - " + PlayerDataAccess.name(target))));
            return;
        }
        if (slotId == 49) {
            viewer.openMenu(new SimpleMenuProvider((containerId, playerInventory, ignored) ->
                new SnapshotCategoryMenu(containerId, playerInventory, viewer, target, mode),
                Component.literal(mode.title() + " Categories - " + PlayerDataAccess.name(target))));
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
        int start = page * 45;
        int end = Math.min(start + 45, snapshotIndexes.size());
        for (int i = start; i < end; i++) {
            int slot = i - start;
            int snapshotIndex = snapshotIndexes.get(i);
            PlayerSnapshot snapshot = InventoryRollbackSavedData.get(PlayerDataAccess.server(target)).getSnapshots(target.getUUID()).get(snapshotIndex);
            ItemStack stack = new ItemStack(Items.CHEST);
            stack.set(DataComponents.CUSTOM_NAME, Component.literal("#" + (snapshotIndex + 1) + " " + FORMATTER.format(Instant.ofEpochMilli(snapshot.timestamp()))));
            container.setItem(slot, stack);
            slotToSnapshotIndex.put(slot, snapshotIndex);
        }
        container.setItem(49, named(Items.BARRIER, "Back to Categories"));
    }

    private static List<Integer> collectIndexes(ServerPlayer target, SnapshotType snapshotType) {
        List<PlayerSnapshot> snapshots = InventoryRollbackSavedData.get(PlayerDataAccess.server(target)).getSnapshots(target.getUUID());
        java.util.ArrayList<Integer> indexes = new java.util.ArrayList<>();
        for (int i = 0; i < snapshots.size(); i++) {
            if (snapshots.get(i).type() == snapshotType) {
                indexes.add(i);
            }
        }
        return indexes;
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
