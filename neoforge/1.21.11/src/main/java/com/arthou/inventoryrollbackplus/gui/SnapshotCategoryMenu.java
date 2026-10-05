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
import net.minecraft.world.item.component.ResolvableProfile;

import java.util.List;

public final class SnapshotCategoryMenu extends AbstractContainerMenu {
    private static final SnapshotType[] DISPLAY_TYPES = {
        SnapshotType.JOIN,
        SnapshotType.DEATH,
        SnapshotType.DIMENSION_CHANGE,
        SnapshotType.MANUAL
    };

    private final ServerPlayer viewer;
    private final ServerPlayer target;
    private final SnapshotViewMode mode;
    private final SimpleContainer container;

    public SnapshotCategoryMenu(int containerId, Inventory playerInventory, ServerPlayer viewer, ServerPlayer target, SnapshotViewMode mode) {
        super(MenuType.GENERIC_9x1, containerId);
        this.viewer = viewer;
        this.target = target;
        this.mode = mode;
        this.container = new SimpleContainer(9);

        container.setItem(0, buildPlayerHead(target));
        for (int i = 0; i < DISPLAY_TYPES.length; i++) {
            SnapshotType type = DISPLAY_TYPES[i];
            container.setItem(3 + i, buildCategoryItem(type));
        }

        for (int col = 0; col < 9; col++) {
            addSlot(new LockedSlot(container, col, 8 + col * 18, 18));
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 50 + row * 18));
            }
        }

        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 108));
        }
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId >= 3 && slotId <= 6) {
            SnapshotType type = DISPLAY_TYPES[slotId - 3];
            viewer.openMenu(new SimpleMenuProvider((containerId, playerInventory, ignored) ->
                new SnapshotEntriesMenu(containerId, playerInventory, viewer, target, type, 0, mode),
                Component.literal(mode.title() + " - " + SnapshotPresentation.displayName(type) + " - " + PlayerDataAccess.name(target))));
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

    private ItemStack buildPlayerHead(ServerPlayer target) {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(PlayerDataAccess.name(target)));
        stack.set(DataComponents.PROFILE, ResolvableProfile.createResolved(target.getGameProfile()));
        return stack;
    }

    private ItemStack buildCategoryItem(SnapshotType type) {
        List<PlayerSnapshot> snapshots = InventoryRollbackSavedData.get(PlayerDataAccess.server(target)).getSnapshots(target.getUUID()).stream()
            .filter(snapshot -> snapshot.type() == type)
            .toList();
        ItemStack stack = new ItemStack(SnapshotPresentation.icon(type));
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(SnapshotPresentation.displayName(type) + " (" + snapshots.size() + ")"));
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
