package org.kilocraft.essentials.config.main.sections;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public class ServerConfigSection {

    @Setting(value = "name")
    @Comment("Name of the server")
    public String name = "Minecraft Server";

    @Setting(value = "logCommands")
    @Comment("If set to true the all the commands entered by players (and RCon) will be logged")
    public boolean logCommands = false;

    @Setting(value = "displayBrandName")
    @Comment("The display brand name, you'll usually see this in the Debug (F3) menu/screen, Default: \"default\"")
    public String displayBrandName = "default";

    @Setting(value = "cooldown")
    @Comment("The time you have to wait before you can teleport")
    public int cooldown = 0;

    @Setting(value = "minTeleportDistance")
    @Comment("The minimum distance you have to be away from your destination to teleport to it")
    public int minTeleportDistance = -1;

}
