package com.arthou.inventoryrollbackplus.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;

public final class PlayerDataAccess {
    private static final EquipmentSlot[] ARMOR_SLOTS = {
        EquipmentSlot.FEET,
        EquipmentSlot.LEGS,
        EquipmentSlot.CHEST,
        EquipmentSlot.HEAD
    };

    private PlayerDataAccess() {
    }

    public static MinecraftServer server(ServerPlayer player) {
        return player.level().getServer();
    }

    public static String name(ServerPlayer player) {
        return player.getGameProfile().name();
    }

    public static CompoundTag save(ServerPlayer player) {
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, server(player).registryAccess());
        player.saveWithoutId(output);
        return output.buildResult();
    }

    public static void load(ServerPlayer player, CompoundTag tag) {
        player.load(TagValueInput.create(ProblemReporter.DISCARDING, server(player).registryAccess(), tag));
    }

    public static ItemStack[] copyMain(ServerPlayer player) {
        ItemStack[] items = new ItemStack[36];
        for (int i = 0; i < items.length; i++) {
            items[i] = player.getInventory().getItem(i).copy();
        }
        return items;
    }

    public static ItemStack[] copyArmor(ServerPlayer player) {
        ItemStack[] armor = new ItemStack[4];
        for (int i = 0; i < armor.length; i++) {
            armor[i] = player.getItemBySlot(ARMOR_SLOTS[i]).copy();
        }
        return armor;
    }

    public static ItemStack[] copyOffhand(ServerPlayer player) {
        return new ItemStack[] {player.getItemBySlot(EquipmentSlot.OFFHAND).copy()};
    }

    public static void setMain(ServerPlayer player, int slot, ItemStack stack) {
        player.getInventory().setItem(slot, stack);
    }

    public static void setArmor(ServerPlayer player, int slot, ItemStack stack) {
        player.setItemSlot(ARMOR_SLOTS[slot], stack);
    }

    public static void setOffhand(ServerPlayer player, ItemStack stack) {
        player.setItemSlot(EquipmentSlot.OFFHAND, stack);
    }
}
