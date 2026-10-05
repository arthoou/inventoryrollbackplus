package com.arthou.inventoryrollbackplus.data;

import net.minecraft.nbt.CompoundTag;

public record PlayerSnapshot(long timestamp, SnapshotType type, String detail, CompoundTag playerData, CompoundTag compatData) {
    public CompoundTag save() {
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
            type = SnapshotType.valueOf(tag.getStringOr("type", SnapshotType.MANUAL.name()));
        } catch (IllegalArgumentException ignored) {
            type = SnapshotType.MANUAL;
        }
        return new PlayerSnapshot(
            tag.getLongOr("timestamp", 0L),
            type,
            tag.getStringOr("detail", ""),
            tag.getCompoundOrEmpty("playerData"),
            tag.getCompoundOrEmpty("compatData")
        );
    }
}
