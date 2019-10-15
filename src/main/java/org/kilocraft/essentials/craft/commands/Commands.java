package org.kilocraft.essentials.craft.commands;

import java.util.ArrayList;
import java.util.List;

public class Commands {
    public static String vanillaCommandsPrefix = "minecraft:";

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
    }};

    public static List<String> vanillaCommandsToRename = new ArrayList<String>(){{
        add("gamemode");
        add("kill");
        add("help");
        add("kick");
        add("pardon");
        add("pardon-ip");
        add("locate");
        add("say");
        add("banlist");
        add("op");
        add("deop");
        add("me");
        add("w");
        add("msg");
        add("whisper");
        add("tell");
        add("teammsg");
        add("tm");
        add("xp");
    }};

    public static boolean isVanillaCommand(String nodeName) {
        if  (vanillaCommandsToRename.contains(vanillaCommandsPrefix + nodeName)) return true;
        else return false;
    }

    public static boolean isCustomCommand(String nodeName) {
        if  (keCommandsToKeep.contains(nodeName)) return true;
        else return false;
    }

}
