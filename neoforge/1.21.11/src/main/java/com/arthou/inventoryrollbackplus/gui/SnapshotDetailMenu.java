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

import java.util.List;

public final class SnapshotDetailMenu extends AbstractContainerMenu {
    private final ServerPlayer viewer;
    private final ServerPlayer target;
    private final int snapshotIndex;
    private final boolean enderChestView;
    private final SnapshotType sourceType;
    private final int sourcePage;
    private final SnapshotViewMode mode;
    private final List<PlayerSnapshot> snapshots;
    private final SimpleContainer previewContainer;

    public SnapshotDetailMenu(int containerId, Inventory playerInventory, ServerPlayer viewer, ServerPlayer target, int snapshotIndex, boolean enderChestView, SnapshotType sourceType, int sourcePage, SnapshotViewMode mode) {
        super(MenuType.GENERIC_9x6, containerId);
        this.viewer = viewer;
        this.target = target;
        this.snapshotIndex = snapshotIndex;
        this.enderChestView = enderChestView;
        this.sourceType = sourceType;
        this.sourcePage = sourcePage;
        this.mode = mode;
        this.snapshots = InventoryRollbackSavedData.get(PlayerDataAccess.server(target)).getSnapshots(target.getUUID());
        this.previewContainer = new SimpleContainer(54);

        SnapshotViewData data = SnapshotViewData.fromSnapshot(PlayerDataAccess.server(target), target.getGameProfile(), snapshots.get(snapshotIndex));
        populatePreview(data);

        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 9; col++) {
                int slotIndex = col + row * 9;
                addSlot(new PreviewSlot(previewContainer, slotIndex, 8 + col * 18, 18 + row * 18));
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
        if (slotId == 45) {
            restorePrimary();
            return;
        }
        if (mode == SnapshotViewMode.VANILLA && slotId == 46) {
            restoreInventoryOnly();
            return;
        }
        if (mode == SnapshotViewMode.VANILLA && slotId == 47) {
            restoreEnderChestOnly();
            return;
        }
        if (mode == SnapshotViewMode.VANILLA && slotId == 49) {
            viewer.openMenu(new SimpleMenuProvider((containerId, playerInventory, ignored) ->
                new SnapshotDetailMenu(containerId, playerInventory, viewer, target, snapshotIndex, !enderChestView, sourceType, sourcePage, mode),
                Component.literal((enderChestView ? "Inventory Backup" : "Ender Chest Backup"))));
            return;
        }
        if (slotId == 51) {
            openTransferMenu();
            return;
        }
        if (slotId == 53) {
            viewer.openMenu(new SimpleMenuProvider((containerId, playerInventory, ignored) ->
                new SnapshotEntriesMenu(containerId, playerInventory, viewer, target, sourceType, sourcePage, mode),
                Component.literal(mode.title() + " - " + PlayerDataAccess.name(target))));
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

    private void restorePrimary() {
        PlayerSnapshot snapshot = snapshots.get(snapshotIndex);
        switch (mode) {
            case VANILLA -> {
                PlayerDataAccess.load(target, snapshot.playerData().copy());
                target.inventoryMenu.broadcastChanges();
                target.containerMenu.broadcastChanges();
                viewer.sendSystemMessage(Component.literal("Restored vanilla backup #" + (snapshotIndex + 1) + " for " + PlayerDataAccess.name(target) + "."));
                target.sendSystemMessage(Component.literal("Your inventory and status were restored from a backup."));
            }
            case CURIOS -> {
                CompatInventorySupport.applyCuriosOnly(target, snapshot.compatData());
                viewer.sendSystemMessage(Component.literal("Restored curios backup #" + (snapshotIndex + 1) + " for " + PlayerDataAccess.name(target) + "."));
            }
            case COSMETIC -> {
                CompatInventorySupport.applyCosmeticOnly(target, snapshot.compatData());
                viewer.sendSystemMessage(Component.literal("Restored cosmetic armor backup #" + (snapshotIndex + 1) + " for " + PlayerDataAccess.name(target) + "."));
            }
        }
    }

    private void restoreInventoryOnly() {
        SnapshotViewData data = SnapshotViewData.fromSnapshot(PlayerDataAccess.server(target), target.getGameProfile(), snapshots.get(snapshotIndex));
        for (int i = 0; i < data.mainInventory().length && i < 36; i++) {
            PlayerDataAccess.setMain(target, i, data.mainInventory()[i].copy());
        }
        for (int i = 0; i < data.armor().length && i < 4; i++) {
            PlayerDataAccess.setArmor(target, i, data.armor()[i].copy());
        }
        if (data.offhand().length > 0) {
            PlayerDataAccess.setOffhand(target, data.offhand()[0].copy());
        }
        target.inventoryMenu.broadcastChanges();
        target.containerMenu.broadcastChanges();
        viewer.sendSystemMessage(Component.literal("Restored inventory backup #" + (snapshotIndex + 1) + " for " + PlayerDataAccess.name(target) + "."));
    }

    private void restoreEnderChestOnly() {
        SnapshotViewData data = SnapshotViewData.fromSnapshot(PlayerDataAccess.server(target), target.getGameProfile(), snapshots.get(snapshotIndex));
        for (int i = 0; i < data.enderChest().length; i++) {
            target.getEnderChestInventory().setItem(i, data.enderChest()[i].copy());
        }
        target.inventoryMenu.broadcastChanges();
        target.containerMenu.broadcastChanges();
        viewer.sendSystemMessage(Component.literal("Restored ender chest backup #" + (snapshotIndex + 1) + " for " + PlayerDataAccess.name(target) + "."));
    }

    private void populatePreview(SnapshotViewData data) {
        switch (mode) {
            case VANILLA -> populateVanillaPreview(data);
            case CURIOS -> populateSimplePreview(data.curios(), "Restore Curios");
            case COSMETIC -> populateSimplePreview(data.cosmeticArmor(), "Restore Cosmetic Armor");
        }
        previewContainer.setItem(53, named(Items.BARRIER, "Back to List"));
    }

    private void populateVanillaPreview(SnapshotViewData data) {
        if (enderChestView) {
            for (int i = 0; i < data.enderChest().length && i < 45; i++) {
                previewContainer.setItem(i, data.enderChest()[i].copy());
            }
        } else {
            for (int i = 0; i < data.mainInventory().length && i < 36; i++) {
                previewContainer.setItem(i, data.mainInventory()[i].copy());
            }
            for (int i = 0; i < data.armor().length && i < 4; i++) {
                previewContainer.setItem(36 + i, data.armor()[i].copy());
            }
            if (data.offhand().length > 0) {
                previewContainer.setItem(40, data.offhand()[0].copy());
            }
        }
        previewContainer.setItem(45, named(Items.LIME_WOOL, "Restore Full Backup"));
        previewContainer.setItem(46, named(Items.CHEST, "Restore Inventory"));
        previewContainer.setItem(47, named(Items.ENDER_CHEST, "Restore Ender Chest"));
        previewContainer.setItem(49, named(enderChestView ? Items.CHEST : Items.ENDER_CHEST, enderChestView ? "Show Inventory Backup" : "Show Ender Chest Backup"));
        previewContainer.setItem(51, named(Items.HOPPER, "Transfer To Player"));
    }

    private void populateSimplePreview(ItemStack[] stacks, String restoreLabel) {
        for (int i = 0; i < stacks.length && i < 45; i++) {
            previewContainer.setItem(i, stacks[i].copy());
        }
        previewContainer.setItem(45, named(Items.LIME_WOOL, restoreLabel));
        previewContainer.setItem(51, named(Items.HOPPER, "Transfer To Player"));
    }

    private void openTransferMenu() {
        PlayerSnapshot snapshot = snapshots.get(snapshotIndex);
        viewer.openMenu(new SimpleMenuProvider((containerId, playerInventory, ignored) ->
            new TransferTargetMenu(containerId, playerInventory, viewer, target, snapshot, snapshotIndex, enderChestView, sourceType, sourcePage, mode),
            Component.literal("Transfer To Player")));
    }

    private static ItemStack named(net.minecraft.world.item.Item item, String name) {
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(name));
        return stack;
    }

    private static final class PreviewSlot extends Slot {
        private PreviewSlot(SimpleContainer container, int slot, int x, int y) {
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
