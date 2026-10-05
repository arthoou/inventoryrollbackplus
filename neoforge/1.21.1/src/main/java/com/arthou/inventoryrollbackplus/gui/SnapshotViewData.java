package com.arthou.inventoryrollbackplus.gui;

import com.arthou.inventoryrollbackplus.data.PlayerSnapshot;
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
        ghost.load(tag);
        CompatInventorySupport.CompatPreviewData compatData = CompatInventorySupport.preview(server, snapshot.compatData());
        return new SnapshotViewData(
            ghost.getInventory().items.stream().map(ItemStack::copy).toArray(ItemStack[]::new),
            ghost.getInventory().armor.stream().map(ItemStack::copy).toArray(ItemStack[]::new),
            ghost.getInventory().offhand.stream().map(ItemStack::copy).toArray(ItemStack[]::new),
            ghost.getEnderChestInventory().getItems().stream().map(ItemStack::copy).toArray(ItemStack[]::new),
            compatData.cosmeticArmor(),
            compatData.curios()
        );
    }
}
