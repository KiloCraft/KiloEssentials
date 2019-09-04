package org.kilocraft.essentials.craft;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.TestCommand;
import org.kilocraft.essentials.craft.commands.*;
import org.kilocraft.essentials.craft.commands.Essentials.*;
import org.kilocraft.essentials.craft.commands.donatorcommands.PlayerParticlesCommand;
import org.kilocraft.essentials.craft.commands.staffcommands.ReloadCommand;
import org.kilocraft.essentials.craft.commands.staffcommands.ServerModNameCommand;

public class KiloCommands {
    private static CommandDispatcher<ServerCommandSource> dispatcher;
    public KiloCommands(CommandDispatcher<ServerCommandSource> serverCommandSourceCommandDispatcher) {
        dispatcher = serverCommandSourceCommandDispatcher;
    }

    public static void register(boolean devEnv) {
        if (devEnv) {
            TestCommand.register(dispatcher);
        }

        // Staff
        VersionCommand.register(dispatcher);
        ReloadCommand.register(dispatcher);
        RankCommand.register(dispatcher);
        ServerModNameCommand.register(dispatcher);
        
        // Donators
        PlayerParticlesCommand.register(dispatcher);
        LocateBiomeCommand.register(dispatcher);
        EnderchestCommand.register(dispatcher);
        CraftingbenchCommand.register(dispatcher);
        AnvilCommand.register(dispatcher);
        RenameCommand.register(dispatcher);
    }

}
