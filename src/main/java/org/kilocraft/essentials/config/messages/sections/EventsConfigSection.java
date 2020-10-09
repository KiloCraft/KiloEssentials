package org.kilocraft.essentials.config.messages.sections;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class EventsConfigSection {

    @Setting(value = "disableOnProxyMode", comment = "If set to true and the Proxy Mode is also enabled the event messages will be disabled")
    public boolean disableOnProxyMode = false;

    @Setting(value = "userJoin")
    public String userJoin = "&e%user_displayname%&e joined the game";

    @Setting(value = "userLeave")
    public String userLeave = "&e%user_displayname%&e left the game.";

}
