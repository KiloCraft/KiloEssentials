package org.kilocraft.essentials;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.TestCommand;
import org.kilocraft.essentials.commands.*;

public class KiloCommands {
    private static CommandDispatcher<ServerCommandSource> dispatcher;
    public KiloCommands(CommandDispatcher<ServerCommandSource> serverCommandSourceCommandDispatcher) {
        dispatcher = serverCommandSourceCommandDispatcher;
    }

    public static void register(boolean devEnv) {
        if (devEnv) {
            TestCommand.register(dispatcher);
        }

        VersionCommand.register(dispatcher);
        ReloadCommand.register(dispatcher);
        RankCommand.register(dispatcher);
        PlayerParticlesCommand.register(dispatcher);
    }

}
