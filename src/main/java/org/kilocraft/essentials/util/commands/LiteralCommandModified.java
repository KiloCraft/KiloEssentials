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
        this.add("ke_gamemode");
        this.add("ke_reload");
        this.add("ke_locate");
        this.add("ke_op");
        this.add("ke_tp");
        this.add("ke_msg");
        this.add("ke_whisper");
        this.add("ke_tell");
        this.add("ke_time");
        this.add("ke_ban");
        this.add("ke_ban");
        this.add("ke_kick");
        this.add("ke_gamerule");
        this.add("ke_ban-ip");
    }};

    private static final List<String> vanillaCommandsToRename = new ArrayList<String>() {{
        this.add("gamemode");
        this.add("locate");
        this.add("me");
        this.add("w");
        this.add("msg");
        this.add("whisper");
        this.add("tell");
        this.add("teammsg");
        this.add("tm");
        this.add("time");
        this.add("locatebiome");
        this.add("ban");
        this.add("ban-ip");
        this.add("seed");
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
