package org.kilocraft.essentials.Commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;

public class CommandsManager {
    private static CommandDispatcher<ServerCommandSource> dispatcher = new CommandDispatcher<>();

    public static void register() {
        VersionCommand.register(dispatcher);

    }

}
