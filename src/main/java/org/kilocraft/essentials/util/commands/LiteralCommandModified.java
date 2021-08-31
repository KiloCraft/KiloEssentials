package org.kilocraft.essentials.util.commands;

import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import org.kilocraft.essentials.mixin.patch.technical.CommandManagerMixin;

import java.util.ArrayList;
import java.util.List;

public class LiteralCommandModified {
    private static final String nmsCommandNamePrefix = "minecraft:";
    private static final String keCommandPrefix = "ke_";

    /**
     * @see CommandManagerMixin
     * <p>
     * This only works for Command literals and their sub commands
     */
    private static final List<String> keCommandsToKeep = new ArrayList<>() {{
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
        add("ke_gamerule");
        add("ke_ban-ip");
    }};

    private static final List<String> vanillaCommandsToRename = new ArrayList<String>() {{
        add("gamemode");
        add("locate");
        add("me");
        add("w");
        add("msg");
        add("whisper");
        add("tell");
        add("teammsg");
        add("tm");
        add("time");
        add("locatebiome");
        add("ban");
        add("ban-ip");
        add("seed");
    }};

    public static String normalizeName(String name) {
        if (keCommandsToKeep.contains(name)) {
            return getKECommandName(name);
        }

        return name;
    }

    public static boolean shouldRenameVanillaCommand(String nodeName) {
        return vanillaCommandsToRename.contains(nodeName);
    }

    public static boolean shouldRenameCustomCommand(String nodeName) {
        return keCommandsToKeep.contains(nodeName);
    }

    public static String getNMSCommandName(String vanillaName) {
        return nmsCommandNamePrefix + vanillaName;
    }

    public static String getKECommandName(String defaultName) {
        return defaultName.replace(keCommandPrefix, "");
    }

    public static String getNMSCommandPrefix() {
        return nmsCommandNamePrefix;
    }

    public static boolean shouldUse(String name) {
        if (shouldRenameCustomCommand(keCommandPrefix + name) &&
                shouldRenameVanillaCommand(name.replace(nmsCommandNamePrefix, "")))
            return true;

        return shouldRenameCustomCommand(keCommandPrefix + name) ||
                !vanillaCommandsToRename.contains(name.replace(nmsCommandNamePrefix, ""));
    }

    public static <S> boolean canSourceUse(CommandNode<S> commandNode, S source) {
        if (commandNode instanceof LiteralCommandNode)
            return shouldUse(commandNode.getName()) && commandNode.canUse(source);

        return commandNode.canUse(source);
    }

}
