package org.kilocraft.essentials.api.command.commandImpl;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.command.CommandRegistry;

import java.util.ArrayList;
import java.util.List;

public class CommandRegistryImpl implements CommandRegistry {
    private List<CommandDispatcher<ServerCommandSource>> commands = new ArrayList<>();

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        commands.add(dispatcher);
    }

    public List<CommandDispatcher<ServerCommandSource>> getCommands() {
        return commands;
    }
}
