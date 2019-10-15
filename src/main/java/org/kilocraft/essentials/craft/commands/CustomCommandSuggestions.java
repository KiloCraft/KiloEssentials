package org.kilocraft.essentials.craft.commands;

import com.mojang.brigadier.tree.CommandNode;

public class CustomCommandSuggestions {
    public static <S> boolean buildForSource(CommandNode<S> commandNode, S source) {
        if (Commands.isVanillaCommand(commandNode.getName().replace(Commands.vanillaCommandsPrefix, "")) && !Commands.isCustomCommand(Commands.customCommandsPrefix + commandNode.getName())) {
            return false;
        }
        if (Commands.isCustomCommand(commandNode.getName())) {
            return true;
        } else {
            return true;
        }
    }

}