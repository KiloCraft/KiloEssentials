package org.kilocraft.essentials.config.messages.sections.commands;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class WarpCommandConfigSection {

    @Setting(value = "teleportTo", comment = "Local Variables: {WARP_NAME}")
    public String teleportTo = "&eTeleporting to &6{WARP_NAME}";

}
