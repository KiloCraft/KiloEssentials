package org.kilocraft.essentials.api.commands;

import com.mojang.brigadier.CommandDispatcher;
import io.github.indicode.fabric.permissions.PermChangeBehavior;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.server.command.ServerCommandSource;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class KiloAPICommands {
    private static List<String> initializedPerms = new ArrayList<>();
    public static String getCommandPermission(String command) {
        if (!initializedPerms.contains(command)) {
            initializedPerms.add(command);
        }
        return "kiloapi.command." + command;
    }
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {

        TpsCommand.register(dispatcher);
        ModsCommand.register(dispatcher);

//        TriggerEventApiCmd.register(dispatcher);
        Thimble.permissionWriters.add((map, server) -> {
            initializedPerms.forEach(perm -> map.registerPermission("kiloapi.command." + perm, PermChangeBehavior.UPDATE_COMMAND_TREE));
        });
    }
}
