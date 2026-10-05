package com.arthou.inventoryrollbackplus.gui;

import com.arthou.inventoryrollbackplus.data.PlayerSnapshot;
import com.arthou.inventoryrollbackplus.data.SnapshotType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public final class SnapshotPresentation {
    private SnapshotPresentation() {
    }

    public static String displayName(PlayerSnapshot snapshot) {
        return displayName(snapshot.type());
    }

    public static String displayName(SnapshotType type) {
        return switch (type) {
            case DEATH -> "Death";
            case JOIN -> "Join";
            case QUIT -> "Quit Backup";
            case DIMENSION_CHANGE -> "World Change";
            case MANUAL -> "Forced Backup";
        };
    }

    public static Item icon(PlayerSnapshot snapshot) {
        return icon(snapshot.type());
    }

    public static Item icon(SnapshotType type) {
        return switch (type) {
            case DEATH -> Items.BONE;
            case JOIN -> Items.OAK_SAPLING;
            case QUIT -> Items.GRAY_DYE;
            case DIMENSION_CHANGE -> Items.FLINT;
            case MANUAL -> Items.DIAMOND;
        };
    }

    public static String detailText(PlayerSnapshot snapshot) {
        String detail = snapshot.detail();
        if (detail == null || detail.isBlank()) {
            return "";
        }
        return switch (snapshot.type()) {
            case DEATH -> "Cause: " + detail;
            case DIMENSION_CHANGE -> "Target: " + detail;
            default -> detail;
        };
    }
}
