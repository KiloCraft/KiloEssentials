package org.kilocraft.essentials;

public enum CommandPermission {
    HOME_SELF_TP("home.self.tp"),
    HOME_SELF_SET("home.self.set"),
    HOME_SELF_REMOVE("home.self.remove"),
    HOMES_SELF("homes.self"),
    HOMES_OTHERS("homes.others"),
    HOME_OTHERS_TP("home.others.tp"),
    HOME_OTHERS_SET("home.others.set"),
    HOME_OTHERS_REMOVE("home.others.remove"),
    HOME_SET_LIMIT("home.set.limit"),
    HOME_SET_LIMIT_BYPASS("home.set.limit.bypass"),
    RELOAD("reload"),
    GAMEMODE("gamemode"),
    GAMEMODE_SELF_SURVIVAL("gamemode.self.survival"),
    GAMEMODE_SELF_ADVENTURE("gamemode.self.adventure"),
    GAMEMODE_SELF_CREATIVE("gamemode.self.creative"),
    GAMEMODE_SELF_SPECTATOR("gamemode.self.spectator"),
    GAMEMODE_OTHERS_SURVIVAL("gamemode.others.survival"),
    GAMEMODE_OTHERS_ADVENTURE("gamemode.others.adventure"),
    GAMEMODE_OTHERS_CREATIVE("gamemode.others.creative"),
    GAMEMODE_OTHERS_SPECTATOR("gamemode.others.spectator"),
    KILL_SINGLE("kill.single"),
    KILL_MULTIPLE("kill.multiple"),
    SUDO_OTHERS("sudo.others"),
    SUDO_CONSOLE("sudo.console"),
    BROADCAST("broadcast"),
    ANVIL("anvil"),
    ITEM("item"),
    LOCATE("locate"),
    LOCATE_BIOME("locate.biome"),
    LOCATE_STRUCTURE("locate.structure"),
    BACK_SELF("back.self"),
    BACK_OTHERS("back.others"),
    HEAL_SELF("heal.self"),
    HEAL_OTHERS("heal.others"),
    FEED_SELF("feed.self"),
    FEED_OTHERS("feed.others"),
    TIME("time"),
    FLY_SELF("fly.self"),
    FLY_OTHERS("fly.others"),
    INVULNERAVLE("invulnerable"),
    TELEPORTTO("teleportto"),
    TELEPORTPOS("teleportpos"),
    TELEPORTHERE("teleporthere"),
    TELEPORTIN("teleportin"),
    NICKNAME_SELF("nickname.self"),
    NICKNAME_OTHERS("nickname.others"),
    NICKNAME_FORMATTING("nickname.formatting"),
    CLEARCHAT("clearchat"),
    ENDERCHEST_SELF("enderchest.self"),
    ENDERCHEST_OTHERS("enderchest.others"),
    ;

    private String node;
    private CommandPermission(String string) {
        this.node = string;
    }

    public String getNode() {
        return KiloCommands.PERMISSION_PREFIX + node;
    }

    public static CommandPermission byName(String name) {
        for (CommandPermission value : CommandPermission.values()) {
            if (value.node.equals(name.toUpperCase()))
                return value;
        }

        return null;
    }
}
