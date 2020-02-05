package org.kilocraft.essentials.commands;

import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import org.kilocraft.essentials.mixin.CommandManagerMixin;

import java.util.ArrayList;
import java.util.List;

public class LiteralCommandModified {
    private static String NMSCommandNamePrefix = "minecraft:";
    private static String keCommandPrefix = "ke_";

    /**
     * @see CommandManagerMixin
     *
     * This only works for Command literals and their sub commands
     */
    private static List<String> keCommandsToKeep = new ArrayList<String>(){{
        add("ke_gamemode");
        add("ke_reload");
        add("ke_locate");
        add("ke_op");
        add("ke_tp");
        add("ke_msg");
        add("ke_whisper");
        add("ke_tell");
        add("ke_time");
        add("ke_ban");
        add("ke_ban");
        add("ke_kick");
        add("ke_help");
        add("ke_gamerule");
    }};

    private static List<String> vanillaCommandsToRename = new ArrayList<String>(){{
        add("gamemode");
        add("help");
        add("locate");
        add("op");
        add("deop");
        add("me");
        add("w");
        add("msg");
        add("whisper");
        add("tell");
        add("teammsg");
        add("tm");
        add("time");
        add("ban");
        add("kick");
        add("ban-ip");
        add("pardon");
        add("pardon-ip");
        add("enchant");
        add("gamerule");
    }};

    public static boolean isVanillaCommand(String nodeName) {
        return vanillaCommandsToRename.contains(nodeName);
    }

    public static boolean isCustomCommand(String nodeName) {
        return keCommandsToKeep.contains(nodeName);
    }

    public static String getNMSCommandName(String vanillaName) {
        return NMSCommandNamePrefix + vanillaName;
    }

    public static String getKECommandName(String defaultName) {
        return defaultName.replace(keCommandPrefix, "");
    }

    public static String getNMSCommandPrefix() {
        return NMSCommandNamePrefix;
    }

    public static boolean shouldUse(String name) {
        if (isCustomCommand(keCommandPrefix + name) &&
                isVanillaCommand(name.replace(NMSCommandNamePrefix, "")))
            return true;

        return isCustomCommand(keCommandPrefix + name) ||
                !vanillaCommandsToRename.contains(name.replace(NMSCommandNamePrefix, ""));
    }

    public static <S> boolean canSourceUse(CommandNode<S> commandNode, S source) {
        if (commandNode instanceof LiteralCommandNode)
            return shouldUse(commandNode.getName()) && commandNode.canUse(source);

        return commandNode.canUse(source);
    }

}
