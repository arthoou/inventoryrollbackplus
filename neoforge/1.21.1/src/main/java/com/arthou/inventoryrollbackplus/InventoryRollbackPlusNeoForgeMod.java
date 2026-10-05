package com.arthou.inventoryrollbackplus;

import com.arthou.inventoryrollbackplus.command.InventoryRollbackCommands;
import com.arthou.inventoryrollbackplus.runtime.InventoryRollbackEvents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(InventoryRollbackPlusNeoForgeMod.MOD_ID)
public final class InventoryRollbackPlusNeoForgeMod {
    public static final String MOD_ID = "inventoryrollbackplus";

    public InventoryRollbackPlusNeoForgeMod(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(InventoryRollbackCommands::register);
        NeoForge.EVENT_BUS.addListener(InventoryRollbackEvents::onPlayerLoggedIn);
        NeoForge.EVENT_BUS.addListener(InventoryRollbackEvents::onPlayerLoggedOut);
        NeoForge.EVENT_BUS.addListener(InventoryRollbackEvents::onPlayerDeath);
        NeoForge.EVENT_BUS.addListener(InventoryRollbackEvents::onPlayerChangedDimension);
    }
}
