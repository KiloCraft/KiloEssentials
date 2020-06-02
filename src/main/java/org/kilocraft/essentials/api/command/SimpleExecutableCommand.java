package org.kilocraft.essentials.api.command;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.server.Server;

@FunctionalInterface
public interface SimpleExecutableCommand {
    int execute(ServerCommandSource source, String[] args, Server server) throws CommandSyntaxException;
}
