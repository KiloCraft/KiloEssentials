package org.kilocraft.essentials.config.main.sections;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.ArrayList;
import java.util.List;

@ConfigSerializable
public class PlayerListConfigSection {

    @Setting(value = "useNickNames")
    @Comment("If set to true the server will attempt to load the player nicknames in the list instead of their username")
    public boolean useNicknames = false;

    @Setting(value = "updateRate")
    @Comment("How often tab lists update (every x ticks)")
    public int updateRate = 100;

    @Setting(value = "header")
    @Comment("Sets the (Tab) PlayerList Header")
    public List<String> header = new ArrayList<String>() {{
        this.add("<rainbow>Minecraft Server");
        this.add("<gray>Welcome <gold>%user_displayname%");
    }};

    @Setting(value = "footer")
    @Comment("Sets the (Tab) PlayerList Footer")
    public List<String> footer = new ArrayList<String>() {{
        this.add("<gray>Ping: %user_formatted_ping% <dark_gray>-<gray> Online: <aqua>%user_player_count% <dark_gray>-<gray> TPS: <reset>%server_formatted_tps%");
        this.add("<gray>Use <green>/help<gray> for more info");
    }};

    @Setting(value = "customOrder")
    @Comment("Change the player list order (You need luckperms installed for this to work)")
    public boolean customOrder = false;

    @Setting(value = "topToBottom")
    @Comment("Changed whether the highest or lowest weight should be on top of the list")
    public boolean topToBottom = false;

    public String getHeader() {
        StringBuilder str = new StringBuilder();
        for (String s : this.header) str.append("\n").append(s);
        return str.toString();
    }

    public String getFooter() {
        StringBuilder str = new StringBuilder();
        for (String s : this.footer) str.append("\n").append(s);
        return str.toString();
    }

}
