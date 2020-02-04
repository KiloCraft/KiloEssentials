package org.kilocraft.essentials.config.messages.sections.commands;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class PlayerHomesConfigSection {

    @Setting(value = "reachedLimit")
    public String reachedLimit = "You can't set any more Homes! you have reached the limit";

    @Setting(value = "invalidHome")
    public String invalidHome = "&cCan not find the home specified!";

    @Setting(value = "teleporting")
    public String teleporting = "&eTeleporting to &6%HOME_NAME%";

    @Setting(value = "homeSet")
    public String homeSet = "&aSet the Home &6%HOME_NAME%";

    @Setting(value = "homeRemoved")
    public String homeRemoved = "&cRemoved the Home &6%HOME_NAME%";

    @Setting(value = "noHome")
    public String noHome = "&cYou don't have any Homes!";

    @Setting(value = "admin")
    private PlayerHomesAdminConfigSection playerHomesAdminSection = new PlayerHomesAdminConfigSection();

    public PlayerHomesAdminConfigSection admin() {
        return playerHomesAdminSection;
    }
}
