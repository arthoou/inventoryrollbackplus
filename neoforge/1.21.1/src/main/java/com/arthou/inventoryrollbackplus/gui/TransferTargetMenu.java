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
import net.minecraft.world.item.component.ResolvableProfile;

import java.util.ArrayList;
import java.util.List;

public final class TransferTargetMenu extends AbstractContainerMenu {
    private final ServerPlayer viewer;
    private final ServerPlayer sourceTarget;
    private final PlayerSnapshot snapshot;
    private final int snapshotIndex;
    private final boolean enderChestView;
    private final SnapshotType sourceType;
    private final int sourcePage;
    private final SnapshotViewMode mode;
    private final SimpleContainer container;
    private final List<ServerPlayer> onlinePlayers;

    public TransferTargetMenu(int containerId, Inventory playerInventory, ServerPlayer viewer, ServerPlayer sourceTarget, PlayerSnapshot snapshot, int snapshotIndex, boolean enderChestView, SnapshotType sourceType, int sourcePage, SnapshotViewMode mode) {
        super(MenuType.GENERIC_9x6, containerId);
        this.viewer = viewer;
        this.sourceTarget = sourceTarget;
        this.snapshot = snapshot;
        this.snapshotIndex = snapshotIndex;
        this.enderChestView = enderChestView;
        this.sourceType = sourceType;
        this.sourcePage = sourcePage;
        this.mode = mode;
        this.container = new SimpleContainer(54);
        this.onlinePlayers = new ArrayList<>(viewer.server.getPlayerList().getPlayers());

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
        if (slotId >= 0 && slotId < Math.min(45, onlinePlayers.size())) {
            ServerPlayer destination = onlinePlayers.get(slotId);
            viewer.openMenu(new SimpleMenuProvider((containerId, playerInventory, ignored) ->
                new TransferConfirmMenu(containerId, playerInventory, viewer, sourceTarget, destination, snapshot, snapshotIndex, enderChestView, sourceType, sourcePage, mode),
                Component.literal("Confirm Transfer - " + destination.getGameProfile().getName())));
            return;
        }
        if (slotId == 49) {
            reopenDetail();
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
        int limit = Math.min(45, onlinePlayers.size());
        for (int i = 0; i < limit; i++) {
            ServerPlayer player = onlinePlayers.get(i);
            ItemStack head = new ItemStack(Items.PLAYER_HEAD);
            head.set(DataComponents.CUSTOM_NAME, Component.literal(player.getGameProfile().getName()));
            head.set(DataComponents.PROFILE, new ResolvableProfile(player.getGameProfile()));
            container.setItem(i, head);
        }
        container.setItem(49, named(Items.BARRIER, "Back"));
    }

    private void reopenDetail() {
        viewer.openMenu(new SimpleMenuProvider((containerId, playerInventory, ignored) ->
            new SnapshotDetailMenu(containerId, playerInventory, viewer, sourceTarget, snapshotIndex, enderChestView, sourceType, sourcePage, mode),
            Component.literal(mode.title() + " - " + sourceTarget.getGameProfile().getName())));
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
