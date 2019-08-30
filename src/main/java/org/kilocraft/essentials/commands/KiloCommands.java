package org.kilocraft.essentials.commands;

import net.fabricmc.fabric.api.registry.CommandRegistry;
import org.kilocraft.essentials.KiloEssentials;
import org.kilocraft.essentials.commands.DiscordCommands.DiscordCommand;

public class KiloCommands {

    public static void register() {
        CommandRegistry.INSTANCE.register(true, VersionCommand::register);
        CommandRegistry.INSTANCE.register(true, DiscordCommand::register);
        CommandRegistry.INSTANCE.register(true, ReloadCommand::register);
        CommandRegistry.INSTANCE.register(true, RankCommand::register);
        CommandRegistry.INSTANCE.register(true, DonaterParticlesCommand::register);

        KiloEssentials.getLogger.info("Successfully registered the commands!");
    }

}
