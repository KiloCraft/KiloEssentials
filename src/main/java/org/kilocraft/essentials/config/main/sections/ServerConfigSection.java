package org.kilocraft.essentials.config.main.sections;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class ServerConfigSection {

    @Setting(value = "name", comment = "Name of the server")
    public String name = "Minecraft Server";

    @Setting(value = "proxyMode", comment = "Set to true if you want to use bungeecord/waterfall with this server")
    public boolean proxyMode = false;

}
