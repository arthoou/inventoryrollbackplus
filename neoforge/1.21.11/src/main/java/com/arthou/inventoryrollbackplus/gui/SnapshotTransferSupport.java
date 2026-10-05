package com.arthou.inventoryrollbackplus.gui;

import com.arthou.inventoryrollbackplus.data.PlayerSnapshot;
import com.arthou.inventoryrollbackplus.util.PlayerDataAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class SnapshotTransferSupport {
    private SnapshotTransferSupport() {
    }

    public static void transferAll(ServerPlayer sourceTarget, ServerPlayer destination, PlayerSnapshot snapshot) {
        SnapshotViewData data = SnapshotViewData.fromSnapshot(PlayerDataAccess.server(destination), sourceTarget.getGameProfile(), snapshot);

        for (int i = 0; i < 36; i++) {
            PlayerDataAccess.setMain(destination, i, i < data.mainInventory().length ? data.mainInventory()[i].copy() : ItemStack.EMPTY);
        }
        for (int i = 0; i < 4; i++) {
            PlayerDataAccess.setArmor(destination, i, i < data.armor().length ? data.armor()[i].copy() : ItemStack.EMPTY);
        }
        PlayerDataAccess.setOffhand(destination, data.offhand().length > 0 ? data.offhand()[0].copy() : ItemStack.EMPTY);

        for (int i = 0; i < destination.getEnderChestInventory().getContainerSize(); i++) {
            destination.getEnderChestInventory().setItem(i, i < data.enderChest().length ? data.enderChest()[i].copy() : ItemStack.EMPTY);
        }

        CompatInventorySupport.apply(destination, snapshot.compatData());
        destination.inventoryMenu.broadcastChanges();
        destination.containerMenu.broadcastChanges();
        destination.sendSystemMessage(Component.literal("Your inventory data was transferred from a rollback snapshot."));
    }
}
