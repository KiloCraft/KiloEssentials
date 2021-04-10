package org.kilocraft.essentials.config.main.sections;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class ServerConfigSection {

    @Setting(value = "name", comment = "Name of the server")
    public String name = "Minecraft Server";

    @Setting(value = "logCommands", comment = "If set to true the all the commands entered by players (and RCon) will be logged")
    public boolean logCommands = false;

    @Setting(value = "displayBrandName", comment = "The display brand name, you'll usually see this in the Debug (F3) menu/screen, Default: \"default\"")
    public String displayBrandName = "default";

    @Setting(value = "cooldown", comment = "The time you have to wait before you can teleport")
    public int cooldown = 0;

}
