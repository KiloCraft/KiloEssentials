package org.kilocraft.essentials.craft.commands;

import java.util.ArrayList;
import java.util.List;

public class Commands {
    public static String vanillaCommandsPrefix = "minecraft:";

    public static List<String> keCommandsToKeep = new ArrayList<String>(){{
        add("ke_gamemode");
        add("ke_stop");
        add("ke_reload");
    }};

    public static List<String> vanillaCommandsToRename = new ArrayList<String>(){{
        add("help");
        add("reload");
        add("gamemode");
        add("kill");
        add("kick");
        add("ban");
        add("w");
        add("msg");
        add("whisper");
        add("tell");
    }};

}
