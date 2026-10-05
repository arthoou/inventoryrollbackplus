package com.arthou.inventoryrollbackplus.runtime;

import com.arthou.inventoryrollbackplus.data.InventoryRollbackSavedData;
import com.arthou.inventoryrollbackplus.data.PlayerSnapshot;
import com.arthou.inventoryrollbackplus.data.SnapshotType;
import com.arthou.inventoryrollbackplus.gui.CompatInventorySupport;
import com.arthou.inventoryrollbackplus.util.PlayerDataAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public final class InventoryRollbackEvents {
    private InventoryRollbackEvents() {
    }

    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            saveSnapshot(player, SnapshotType.JOIN, "join");
        }
    }

    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            saveSnapshot(player, SnapshotType.QUIT, "quit");
        }
    }

    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            String detail = event.getSource().typeHolder().unwrapKey()
                .map(key -> key.identifier().toString())
                .orElse("death");
            saveSnapshot(player, SnapshotType.DEATH, detail);
        }
    }

    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            saveSnapshot(player, SnapshotType.DIMENSION_CHANGE, event.getTo().identifier().toString());
        }
    }

    public static void saveSnapshot(ServerPlayer player, SnapshotType type, String detail) {
        CompoundTag playerData = PlayerDataAccess.save(player);
        CompoundTag compatData = CompatInventorySupport.capture(player);
        InventoryRollbackSavedData.get(PlayerDataAccess.server(player)).addSnapshot(
            player.getUUID(),
            new PlayerSnapshot(System.currentTimeMillis(), type, detail, playerData, compatData)
        );
    }
}
