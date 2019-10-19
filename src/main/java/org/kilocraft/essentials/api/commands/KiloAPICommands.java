package org.kilocraft.essentials.api.commands;

import com.mojang.brigadier.CommandDispatcher;
import io.github.indicode.fabric.permissions.Thimble;
import io.github.indicode.fabric.permissions.command.CommandPermission;
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
        Thimble.permissionWriters.add(pair -> {
            initializedPerms.forEach(perm -> {
                try {
                    pair.getLeft().getPermission("kiloapi", CommandPermission.class);
                    pair.getLeft().getPermission("kiloapi.command", CommandPermission.class);
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
                    e.printStackTrace();
                }
                try {
                    pair.getLeft().getPermission("kiloapi.command." + perm, CommandPermission.class);
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
                    e.printStackTrace();
                }
            });
        });
    }
}
