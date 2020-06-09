package org.kilocraft.essentials.config.messages.sections.commands;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class PlayerWarpCommandConfigSection {

    @Setting(value = "warpSet")
    public String warpSet = "&aWarp set! {NAME}";

    @Setting(value = "warpRemoved")
    public String warpRemoved = "&cWarp removed! {NAME}";

    @Setting(value = "nameAlreadyTaken")
    public String nameAlreadyTaken = "&cThat name is already taken!";

    @Setting(value = "limitReached")
    public String limitReached = "&cYou've reached the limit! You can't set any more Warps!";
}
