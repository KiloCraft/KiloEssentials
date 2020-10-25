package org.kilocraft.essentials.config.main.sections;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.ArrayList;
import java.util.List;

@ConfigSerializable
public class PlayerListConfigSection {

    @Setting(value = "useNickNames", comment = "If set to true the server will attempt to load the player nicknames in the list instead of their username")
    public boolean useNicknames = false;

    @Setting(value = "header", comment = "Sets the (Tab) PlayerList Header")
    public List<String> header = new ArrayList<String>(){{
        add("<rainbow>Minecraft Server");
        add("<gray>Welcome <gold>%user_displayname%");
    }};

    @Setting(value = "footer", comment = "Sets the (Tab) PlayerList Footer")
    public List<String> footer = new ArrayList<String>(){{
        add("<gray>Ping: %user_formatted_ping% <dark_gray>-<gray> Online: <aqua>%server_player_count% <dark_gray>-<gray> TPS: <reset>%server_formatted_tps%");
        add("<gray>Use <green>/help<gray> for more info");
    }};

    public String getHeader() {
        StringBuilder str = new StringBuilder();
        for (String s : header) str.append("\n").append(s);
        return str.toString();
    }

    public String getFooter() {
        StringBuilder str = new StringBuilder();
        for (String s : footer) str.append("\n").append(s);
        return str.toString();
    }

}
