package org.kilocraft.essentials.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;

public class CommandsManager {
    private static CommandDispatcher<ServerCommandSource> dispatcher = new CommandDispatcher<>();

    public static void register() {
        VersionCommand.register(dispatcher);

    }

}
