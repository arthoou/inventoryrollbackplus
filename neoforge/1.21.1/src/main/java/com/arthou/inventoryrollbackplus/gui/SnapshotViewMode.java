package com.arthou.inventoryrollbackplus.gui;

public enum SnapshotViewMode {
    VANILLA("Rollback"),
    CURIOS("Curios"),
    COSMETIC("Cosmetic Armor");

    private final String title;

    SnapshotViewMode(String title) {
        this.title = title;
    }

    public String title() {
        return title;
    }
}
