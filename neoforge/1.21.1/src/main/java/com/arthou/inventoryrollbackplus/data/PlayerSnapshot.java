package com.arthou.inventoryrollbackplus.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

public record PlayerSnapshot(long timestamp, SnapshotType type, String detail, CompoundTag playerData, CompoundTag compatData) {
    public CompoundTag save(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putLong("timestamp", timestamp);
        tag.putString("type", type.name());
        tag.putString("detail", detail == null ? "" : detail);
        tag.put("playerData", playerData.copy());
        tag.put("compatData", compatData == null ? new CompoundTag() : compatData.copy());
        return tag;
    }

    public static PlayerSnapshot load(CompoundTag tag) {
        SnapshotType type;
        try {
            type = SnapshotType.valueOf(tag.getString("type"));
        } catch (IllegalArgumentException ignored) {
            type = SnapshotType.MANUAL;
        }
        return new PlayerSnapshot(
            tag.getLong("timestamp"),
            type,
            tag.getString("detail"),
            tag.getCompound("playerData"),
            tag.getCompound("compatData")
        );
    }
}
