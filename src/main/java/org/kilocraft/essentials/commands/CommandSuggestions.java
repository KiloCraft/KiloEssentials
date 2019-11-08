package org.kilocraft.essentials.commands;

import org.kilocraft.essentials.commands.essentials.Commands;

import com.mojang.brigadier.tree.CommandNode;

public class CommandSuggestions {
    public static <S> boolean buildForSource(CommandNode<S> commandNode, S source) {
        if (commandNode.canUse(source)) {
            if (Commands.isVanillaCommand(commandNode.getName().replace(Commands.vanillaCommandsPrefix, ""))
                    && !Commands.isCustomCommand(Commands.customCommandsPrefix + commandNode.getName()))
                return false;
            if (Commands.isCustomCommand(commandNode.getName()))
                return true;
            else
                return true;
        } else
            return false;
    }
}