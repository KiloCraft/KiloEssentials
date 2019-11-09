package org.kilocraft.essentials.commands;

import org.kilocraft.essentials.mixin.CommandManagerMixin;

import java.util.ArrayList;
import java.util.List;

public class Commands {
    public static String vanillaCommandsPrefix = "minecraft:";
    public static String customCommandsPrefix = "ke_";

    /**
     * @see CommandManagerMixin
     *
     * This only works for Command literals and their sub commands
     */
    public static List<String> keCommandsToKeep = new ArrayList<String>(){{
        add("ke_gamemode");
        add("ke_reload");
        add("ke_locate");
        add("ke_op");
        add("ke_kill");
        add("ke_tp");
        add("ke_msg");
        add("ke_whisper");
        add("ke_tell");
        add("ke_time");
        add("ke_ban");
    }};

    public static List<String> vanillaCommandsToRename = new ArrayList<String>(){{
        add("gamemode");
        add("kill");
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
    }};

    public static boolean isVanillaCommand(String nodeName) {
        return Commands.vanillaCommandsToRename.contains(nodeName);
    }

    public static boolean isCustomCommand(String nodeName) {
        return keCommandsToKeep.contains(nodeName);
    }

}
