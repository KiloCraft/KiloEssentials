package org.kilocraft.essentials;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.TestCommand;
import org.kilocraft.essentials.commands.CommandRegistry;
import org.kilocraft.essentials.commands.VersionCommand;

public class KiloCommands {

    public KiloCommands(boolean devEnv) {
        if (devEnv) {
            CommandRegistry.add(TestCommand::register);
        }

        CommandRegistry.add(VersionCommand::register);

        KiloEssentials.getLogger.info("Successfully registered the commands!");
    }

}
