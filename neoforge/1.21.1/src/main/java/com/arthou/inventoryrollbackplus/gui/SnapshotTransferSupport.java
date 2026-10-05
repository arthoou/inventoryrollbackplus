package com.arthou.inventoryrollbackplus.gui;

import com.arthou.inventoryrollbackplus.data.PlayerSnapshot;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class SnapshotTransferSupport {
    private SnapshotTransferSupport() {
    }

    public static void transferAll(ServerPlayer sourceTarget, ServerPlayer destination, PlayerSnapshot snapshot) {
        SnapshotViewData data = SnapshotViewData.fromSnapshot(destination.server, sourceTarget.getGameProfile(), snapshot);

        for (int i = 0; i < 36; i++) {
            destination.getInventory().items.set(i, i < data.mainInventory().length ? data.mainInventory()[i].copy() : net.minecraft.world.item.ItemStack.EMPTY);
        }
        for (int i = 0; i < 4; i++) {
            destination.getInventory().armor.set(i, i < data.armor().length ? data.armor()[i].copy() : net.minecraft.world.item.ItemStack.EMPTY);
        }
        destination.getInventory().offhand.set(0, data.offhand().length > 0 ? data.offhand()[0].copy() : net.minecraft.world.item.ItemStack.EMPTY);

        for (int i = 0; i < destination.getEnderChestInventory().getContainerSize(); i++) {
            destination.getEnderChestInventory().setItem(i, i < data.enderChest().length ? data.enderChest()[i].copy() : net.minecraft.world.item.ItemStack.EMPTY);
        }

        CompatInventorySupport.apply(destination, snapshot.compatData());
        destination.inventoryMenu.broadcastChanges();
        destination.containerMenu.broadcastChanges();
        destination.sendSystemMessage(Component.literal("Your inventory data was transferred from a rollback snapshot."));
    }
}
