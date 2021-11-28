package org.kilocraft.essentials.api.command;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;

@FunctionalInterface
public interface SimpleExecutableCommand {
    int execute(CommandSourceStack source, String[] args) throws CommandSyntaxException;
}
