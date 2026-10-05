package com.arthou.inventoryrollbackplus.gui;

import com.arthou.inventoryrollbackplus.data.PlayerSnapshot;
import com.arthou.inventoryrollbackplus.util.PlayerDataAccess;
import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public record SnapshotViewData(
    ItemStack[] mainInventory,
    ItemStack[] armor,
    ItemStack[] offhand,
    ItemStack[] enderChest,
    ItemStack[] cosmeticArmor,
    ItemStack[] curios
) {
    public static SnapshotViewData fromSnapshot(MinecraftServer server, GameProfile profile, PlayerSnapshot snapshot) {
        ServerPlayer ghost = new ServerPlayer(server, server.overworld(), profile, ClientInformation.createDefault());
        CompoundTag tag = snapshot.playerData().copy();
        PlayerDataAccess.load(ghost, tag);
        CompatInventorySupport.CompatPreviewData compatData = CompatInventorySupport.preview(server, snapshot.compatData());
        return new SnapshotViewData(
            PlayerDataAccess.copyMain(ghost),
            PlayerDataAccess.copyArmor(ghost),
            PlayerDataAccess.copyOffhand(ghost),
            ghost.getEnderChestInventory().getItems().stream().map(ItemStack::copy).toArray(ItemStack[]::new),
            compatData.cosmeticArmor(),
            compatData.curios()
        );
    }
}
