package org.kilocraft.essentials.config.main.sections;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.ArrayList;
import java.util.List;

@ConfigSerializable
public class PlayerListConfigSection {

    @Setting(value = "header", comment = "Sets the (Tab) PlayerList Header")
    public List<String> header = new ArrayList<String>(){{
        add("&aMinecraft Server");
        add("&7Welcome &6%USER_DISPLAYNAME%&r");
    }};

    @Setting(value = "footer", comment = "Sets the (Tab) PlayerList Footer")
    public List<String> footer = new ArrayList<String>(){{
        add("&7Ping: %PLAYER_FORMATTED_PING% &8-&7 Online: &b%SERVER_PLAYER_COUNT% &8-&7 &7TPS: &r%SERVER_FORMATTED_TPS%");
        add("&7Use &3/help&7 for more info");
    }};
}
