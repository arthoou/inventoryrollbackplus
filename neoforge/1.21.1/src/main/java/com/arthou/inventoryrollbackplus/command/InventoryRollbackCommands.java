package com.arthou.inventoryrollbackplus.command;

import com.arthou.inventoryrollbackplus.data.InventoryRollbackSavedData;
import com.arthou.inventoryrollbackplus.data.PlayerSnapshot;
import com.arthou.inventoryrollbackplus.data.SnapshotType;
import com.arthou.inventoryrollbackplus.gui.SnapshotCategoryMenu;
import com.arthou.inventoryrollbackplus.gui.SnapshotViewMode;
import com.arthou.inventoryrollbackplus.runtime.InventoryRollbackEvents;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class InventoryRollbackCommands {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    private InventoryRollbackCommands() {
    }

    public static void register(RegisterCommandsEvent event) {
        var rollbackRoot = Commands.literal("inventoryrollback")
            .requires(source -> source.hasPermission(2))
            .executes(InventoryRollbackCommands::showHelp)
            .then(Commands.literal("forcebackup")
                .then(Commands.argument("target", EntityArgument.player()).executes(InventoryRollbackCommands::forceBackup)))
            .then(Commands.literal("restore")
                .then(Commands.argument("target", EntityArgument.player())
                    .executes(InventoryRollbackCommands::listSnapshots)
                    .then(Commands.argument("index", IntegerArgumentType.integer(1)).executes(InventoryRollbackCommands::restoreSnapshot))))
            .then(Commands.literal("curios")
                .then(Commands.argument("target", EntityArgument.player())
                    .executes(context -> listSnapshots(context, SnapshotViewMode.CURIOS))))
            .then(Commands.literal("cosmetic")
                .then(Commands.argument("target", EntityArgument.player())
                    .executes(context -> listSnapshots(context, SnapshotViewMode.COSMETIC))))
            .then(Commands.literal("latest")
                .then(Commands.argument("target", EntityArgument.player()).executes(InventoryRollbackCommands::restoreLatest)));
        event.getDispatcher().register(rollbackRoot);
    }

    private static int showHelp(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(() -> Component.literal("Use /inventoryrollback forcebackup <player>, /inventoryrollback restore <player>, or /inventoryrollback latest <player>."), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int forceBackup(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "target");
        InventoryRollbackEvents.saveSnapshot(target, SnapshotType.MANUAL, "manual backup");
        context.getSource().sendSuccess(() -> Component.literal("Manual backup created for " + target.getGameProfile().getName() + "."), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int listSnapshots(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return listSnapshots(context, SnapshotViewMode.VANILLA);
    }

    private static int listSnapshots(CommandContext<CommandSourceStack> context, SnapshotViewMode mode) throws CommandSyntaxException {
        ServerPlayer viewer = context.getSource().getPlayerOrException();
        ServerPlayer target = EntityArgument.getPlayer(context, "target");
        List<PlayerSnapshot> snapshots = InventoryRollbackSavedData.get(target.server).getSnapshots(target.getUUID());
        if (snapshots.isEmpty()) {
            context.getSource().sendFailure(Component.literal("No snapshots found for " + target.getGameProfile().getName() + "."));
            return 0;
        }
        viewer.openMenu(new SimpleMenuProvider((containerId, playerInventory, ignored) ->
            new SnapshotCategoryMenu(containerId, playerInventory, viewer, target, mode),
            Component.literal(mode.title() + " Categories - " + target.getGameProfile().getName())));
        return Command.SINGLE_SUCCESS;
    }

    private static int restoreLatest(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "target");
        return restore(target, 1, context.getSource());
    }

    private static int restoreSnapshot(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "target");
        int index = IntegerArgumentType.getInteger(context, "index");
        return restore(target, index, context.getSource());
    }

    private static int restore(ServerPlayer target, int index, CommandSourceStack source) {
        List<PlayerSnapshot> snapshots = InventoryRollbackSavedData.get(target.server).getSnapshots(target.getUUID());
        if (snapshots.isEmpty()) {
            source.sendFailure(Component.literal("No snapshots found for " + target.getGameProfile().getName() + "."));
            return 0;
        }
        if (index < 1 || index > snapshots.size()) {
            source.sendFailure(Component.literal("Invalid snapshot index. Available range: 1-" + snapshots.size()));
            return 0;
        }

        PlayerSnapshot snapshot = snapshots.get(index - 1);
        target.load(snapshot.playerData().copy());
        com.arthou.inventoryrollbackplus.gui.CompatInventorySupport.apply(target, snapshot.compatData());
        target.inventoryMenu.broadcastChanges();
        target.containerMenu.broadcastChanges();
        source.sendSuccess(() -> Component.literal("Restored snapshot #" + index + " for " + target.getGameProfile().getName() + "."), true);
        target.sendSystemMessage(Component.literal("Your inventory and status were restored from a backup."));
        return Command.SINGLE_SUCCESS;
    }
}
