package org.kilocraft.essentials.craft.commands;

import java.util.ArrayList;
import java.util.List;

public class Commands {
    public static String vanillaCommandsPrefix = "minecraft:";

    public static List<String> keCommandsToKeep = new ArrayList<String>(){{
        add("ke_gamemode");
        add("ke_reload");
        add("ke_stop");
        add("ke_locate");
        add("ke_op");
        add("ke_kill");
    }};

    public static List<String> vanillaCommandsToRename = new ArrayList<String>(){{
        add("help");
        add("reload");
        add("gamemode");
        add("kill");
        add("kick");
        add("ban");
        add("ban-ip");
        add("pardon");
        add("pardon-ip");
        add("locate");
        add("debug");
        add("say");
        add("banlist");
        add("clear");
        add("op");
        add("deop");
        add("save-all");
        add("save-off");
        add("save-on");
        add("me");
        add("w");
        add("msg");
        add("whisper");
        add("tell");
    }};

}
