package org.kilocraft.essentials.commands;

import org.kilocraft.essentials.commands.DiscordCommands.DiscordCommand;

import net.fabricmc.fabric.api.registry.CommandRegistry;

public class KiloCommands {
	
    public static void register() {
        CommandRegistry.INSTANCE.register(true, VersionCommand::register);
        CommandRegistry.INSTANCE.register(true, DiscordCommand::register);
        CommandRegistry.INSTANCE.register(true, RankCommand::register);
    }
    
}
