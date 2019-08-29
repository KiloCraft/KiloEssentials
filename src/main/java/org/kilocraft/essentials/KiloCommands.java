package org.kilocraft.essentials;

import org.kilocraft.essentials.commands.DiscordCommands.DiscordCommand;
import org.kilocraft.essentials.commands.RankCommand;
import org.kilocraft.essentials.commands.ReloadCommand;
import org.kilocraft.essentials.commands.VersionCommand;

import net.fabricmc.fabric.api.registry.CommandRegistry;

public class KiloCommands {
    public KiloCommands() {
        CommandRegistry.INSTANCE.register(true, VersionCommand::register);
        CommandRegistry.INSTANCE.register(true, DiscordCommand::register);
        CommandRegistry.INSTANCE.register(true, ReloadCommand::register);
        CommandRegistry.INSTANCE.register(true, RankCommand::register);
    }
}
