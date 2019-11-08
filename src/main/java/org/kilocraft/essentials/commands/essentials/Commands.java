package org.kilocraft.essentials.commands.essentials;

import java.util.ArrayList;
import java.util.List;

public class Commands {
    public static String vanillaCommandsPrefix = "minecraft:";
    public static String customCommandsPrefix = "ke_";

    /**
     * @see org.kilocraft.essentials.craft.mixin.MixinCommandManager
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
    }};

    public static boolean isVanillaCommand(String nodeName) {
        if  (Commands.vanillaCommandsToRename.contains(nodeName)) return true;
        else return false;
    }

    public static boolean isCustomCommand(String nodeName) {
        if  (keCommandsToKeep.contains(nodeName)) return true;
        else return false;
    }

}
