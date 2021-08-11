package org.kilocraft.essentials.api.command;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;

@FunctionalInterface
public interface SimpleExecutableCommand {
    int execute(ServerCommandSource source, String[] args) throws CommandSyntaxException;
}
