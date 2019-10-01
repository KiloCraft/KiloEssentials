package org.kilocraft.essentials.craft.commands;

import java.util.ArrayList;
import java.util.List;

public class Commands {
    public static String keCommandsPrefix = "ke_";
    public static String vanillaCommandsPrefix = "minecraft:";

    public static List<String> keCommandsToRename = new ArrayList<String>(){{
        add("gamemode");
        add("stop");
        add("reload");
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


    public static List<String> vanillaCommandsToRemove = new ArrayList<String>(){{
        add("stop");
    }};
}
