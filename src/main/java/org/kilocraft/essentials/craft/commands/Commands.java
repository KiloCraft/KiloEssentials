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
        add("advancemnt");
        add("bossbar");
        add("clone");
        add("data");
        add("datapack");
        add("defaultgamemode");
        add("difficulty");
        add("effect");
        add("enchant");
        add("execute");
        add("experience");
        add("fill");
        add("forceload");
        add("function");
        add("gamerule");
        add("give");
        add("locate");
        add("loot");
        add("particle");
        add("playsound");
        add("publish");
        add("recipe");
        add("replaceitem");
        add("schedule");
        add("scoreboard");
        add("seed");
        add("setblock");
        add("spawnpoint");
        add("spectate");
        add("spreadplayers");
        add("stop");
        add("stopsound");
        add("summon");
        add("tag");
        add("team");
        add("teammsg");
        add("teleport");
        add("tellraw");
        add("time");
        add("title");
        add("tm");
        add("tp");
        add("trigger");
        add("weather");
        add("whitelist");
        add("worldborder");
        add("xp");
    }};

}
