package org.kilocraft.essentials.api.command;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

public interface CommandedArgument {
    String[] getExamples();

    CommandedArgument parse() throws CommandSyntaxException;

}
