package org.kilocraft.essentials;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.TestCommand;
import org.kilocraft.essentials.commands.*;
import org.kilocraft.essentials.commands.Essentials.AnvilCommand;
import org.kilocraft.essentials.commands.Essentials.CraftingbenchCommand;
import org.kilocraft.essentials.commands.Essentials.EnderchestCommand;
import org.kilocraft.essentials.commands.Essentials.LocateBiomeCommand;
import org.kilocraft.essentials.commands.Essentials.RenameCommand;
import org.kilocraft.essentials.commands.PlayerSpecialCommands.PlayerParticlesCommand;
import org.kilocraft.essentials.commands.ServerControlCommands.ReloadCommand;
import org.kilocraft.essentials.commands.ServerControlCommands.ServerModNameCommand;

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
