package org.kilocraft.essentials.config.messages.sections.commands;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class PlayerHomesAdminConfigSection {

    @Setting(value = "teleporting")
    public String teleporting = "&eTeleporting to &6%HOME_NAME}&e from &b%TARGET_TAG}";

    @Setting(value = "homeSet")
    public String homeSet = "&aSet the Home &6%HOME_NAME}&a for &b%TARGET_TAG}&r";

    @Setting(value = "homeRemoved")
    public String homeRemoved = "&cRemoved the Home &6%HOME_NAME}&c for &b%TARGET_TAG}&r";

    @Setting(value = "noHome")
    public String noHome = "&c%TARGET_TAG}&r&c doesn't have any Homes!";

}
