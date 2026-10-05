package com.arthou.inventoryrollbackplus.data;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class InventoryRollbackSavedData extends SavedData {
    private static final String DATA_NAME = "inventoryrollbackplus_state";
    private static final Codec<InventoryRollbackSavedData> CODEC = CompoundTag.CODEC.xmap(InventoryRollbackSavedData::loadTag, InventoryRollbackSavedData::saveTag);
    private static final SavedDataType<InventoryRollbackSavedData> TYPE = new SavedDataType<>(DATA_NAME, InventoryRollbackSavedData::new, CODEC);
    private static final int DEFAULT_LIMIT = 10;
    private static final int DEATH_LIMIT = 50;

    private final Map<UUID, List<PlayerSnapshot>> snapshotsByPlayer = new HashMap<>();

    public static InventoryRollbackSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    private static InventoryRollbackSavedData loadTag(CompoundTag tag) {
        InventoryRollbackSavedData data = new InventoryRollbackSavedData();
        CompoundTag playersTag = tag.getCompoundOrEmpty("players");
        for (String uuidString : playersTag.keySet()) {
            UUID uuid = parseUuid(uuidString);
            if (uuid == null) {
                continue;
            }
            ListTag listTag = playersTag.getListOrEmpty(uuidString);
            List<PlayerSnapshot> snapshots = new ArrayList<>();
            listTag.compoundStream().map(PlayerSnapshot::load).forEach(snapshots::add);
            snapshots.sort(Comparator.comparingLong(PlayerSnapshot::timestamp).reversed());
            data.snapshotsByPlayer.put(uuid, snapshots);
        }
        return data;
    }

    private CompoundTag saveTag() {
        CompoundTag tag = new CompoundTag();
        CompoundTag playersTag = new CompoundTag();
        snapshotsByPlayer.forEach((uuid, snapshots) -> {
            ListTag listTag = new ListTag();
            for (PlayerSnapshot snapshot : snapshots) {
                listTag.add(snapshot.save());
            }
            playersTag.put(uuid.toString(), listTag);
        });
        tag.put("players", playersTag);
        return tag;
    }

    public void addSnapshot(UUID playerId, PlayerSnapshot snapshot) {
        List<PlayerSnapshot> snapshots = snapshotsByPlayer.computeIfAbsent(playerId, ignored -> new ArrayList<>());
        snapshots.add(0, snapshot);

        int typeCount = 0;
        int limit = snapshot.type() == SnapshotType.DEATH ? DEATH_LIMIT : DEFAULT_LIMIT;
        for (int i = 0; i < snapshots.size(); i++) {
            PlayerSnapshot current = snapshots.get(i);
            if (current.type() == snapshot.type()) {
                typeCount++;
                if (typeCount > limit) {
                    snapshots.remove(i);
                    i--;
                }
            }
        }

        setDirty();
    }

    public List<PlayerSnapshot> getSnapshots(UUID playerId) {
        return snapshotsByPlayer.getOrDefault(playerId, List.of());
    }

    private static UUID parseUuid(String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
