package org.kilocraft.essentials;

import net.fabricmc.fabric.api.registry.CommandRegistry;
import org.kilocraft.essentials.commands.DiscordCommands.DiscordCommand;
import org.kilocraft.essentials.commands.VersionCommand;

public class KiloCommands {
    public KiloCommands() {
        CommandRegistry.INSTANCE.register(true, VersionCommand::register);
        CommandRegistry.INSTANCE.register(true, DiscordCommand::register);


    }
}
