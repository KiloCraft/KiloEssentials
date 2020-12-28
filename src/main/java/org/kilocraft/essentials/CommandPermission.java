package org.kilocraft.essentials;

import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.util.PermissionUtil;

public enum CommandPermission {
    PING_SELF("ping.self"),
    PING_OTHERS("ping.others"),
    WARP("warp"),
    DELWARP("delwarp"),
    SETWARP("setwarp"),
    HOME_SELF_TP("home.self.tp"),
    HOME_SELF_SET("home.self.set"),
    HOME_SELF_REMOVE("home.self.remove"),
    HOMES_SELF("homes.self"),
    HOMES_OTHERS("homes.others"),
    HOME_OTHERS_TP("home.others.tp"),
    HOME_OTHERS_SET("home.others.set"),
    HOME_OTHERS_REMOVE("home.others.remove"),
    HOME_LIMIT("home.limit"),
    HOME_SET_LIMIT_BYPASS("home.limit.bypass"),
    PLAYER_WARP("player_warp"),
    PLAYER_WARP_SELF("player_warp.self"),
    PLAYER_WARP_OTHERS("player_warp.others"),
    PLAYER_WARP_LIMIT("player_warp.limit"),
    PLAYER_WARP_LIMIT_BYPASS("player_warp.limit.bypass"),
    PLAYER_WARPS("playerwarps"),
    RELOAD("reload"),
    GAMEMODE_SELF_SURVIVAL("gamemode.self.survival"),
    GAMEMODE_SELF_ADVENTURE("gamemode.self.adventure"),
    GAMEMODE_SELF_CREATIVE("gamemode.self.creative"),
    GAMEMODE_SELF_SPECTATOR("gamemode.self.spectator"),
    GAMEMODE_OTHERS_SURVIVAL("gamemode.others.survival"),
    GAMEMODE_OTHERS_ADVENTURE("gamemode.others.adventure"),
    GAMEMODE_OTHERS_CREATIVE("gamemode.others.creative"),
    GAMEMODE_OTHERS_SPECTATOR("gamemode.others.spectator"),
    SUDO_OTHERS("sudo.others"),
    SUDO_SERVER("sudo.server"),
    BROADCAST("broadcast"),
    ANVIL("anvil"),
    WORKBENCH("workbench"),
    SMITH("smith"),
    ITEM_FORMATTING("item.formatting"),
    ITEM_NAME("item.name"),
    ITEM_LORE("item.lore"),
    ITEM_ENCHANT("item.enchant"),
    ITEM_COMMANDS("item.commands"),
    ITEM_MEND("item.mend"),
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
    MOBCAP_SET("mobcap.set"),
    MOBCAP_QUERY("mobcap.query"),
    VIEWDISTANCE("viewdistance"),
    NICKNAME_SELF("nickname.self"),
    NICKNAME_OTHERS("nickname.others"),
    REALNAME("realname"),
    CLEARCHAT("clearchat"),
    ENDERCHEST_SELF("enderchest.self"),
    ENDERCHEST_OTHERS("enderchest.others"),
    ENTITIES("entities"),
    ENTITIES_PLAYER("entities.player"),
    STATUS("status"),
    SEEK_INVENTORY("seek_inventory"),
    SAYAS_OTHERS("sayas.others"),
    SAYAS_SERVER("sayas.server"),
    SMITE("smite"),
    SIGNEDIT_TEXT("signedit.text"),
    SIGNEDIT_GUI_SELF("signedit.gui.self"),
    SIGNEDIT_GUI_OTHERS("signedit.gui.others"),
    SIGNEDIT_COMMAND("signedit.command"),
    SIGNEDIT_COLOR("signedit.color"),
    SIGNEDIT_TYPE("signedit.type"),
    HAT_SELF("hat.self"),
    HAT_OTHERS("hat.others"),
    SHOOT("shoot"),
    IPINFO("ipinfo"),
    WHOIS_SELF("whois.self"),
    WHOIS_OTHERS("whois.others"),
    WHOWAS_SELF("whowas.self"),
    WHOWAS_OTHERS("whowas.others"),
    PLAYTIME_SELF("playtime.self"),
    PLAYTIME_OTHERS("playtime.others"),
    PLAYTIME_MODIFY("playtime.modify"),
    PLAYTIMETOP("playtimetop"),
    IGNORELIST_OTHERS("ignorelist.others"),
    BAN("ban"),
    IPBAN("ipban"),
    MUTE("mute"),
    KICK("kick"),
    TELEPORTREQUEST("teleportrequest"),
    LASTSEEN("lastseen"),
    CALCULATE("calculate"),
    GLOW("glow"),
    HUG_USE("hug"),
    HUG_BYPASS("hug.bypass");

    private final String node;

    CommandPermission(String string) {
        this.node = string;
    }

    public String getNode() {
        return PermissionUtil.COMMAND_PERMISSION_PREFIX + node;
    }

    @Nullable
    public static CommandPermission byName(String name) {
        for (CommandPermission value : CommandPermission.values()) {
            if (name.toLowerCase().equals(value.node.toLowerCase())) {
                return value;
            }
        }

        return null;
    }

    @Nullable
    public static CommandPermission get(String string) {
        for (CommandPermission value : CommandPermission.values())
            if (string.toUpperCase().replaceAll(".", "_").equals(value.name()))
                return value;

        return null;
    }

    @Nullable
    public static CommandPermission getByNode(String partOfNode) {
        for (CommandPermission value : CommandPermission.values()) {
            if (value.node.contains(partOfNode))
                return value;
        }

        return null;
    }

}
