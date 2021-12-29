package org.kilocraft.essentials.config.main.sections;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public class MotdConfigSection {

    @Setting(value = "enabled")
    @Comment("Enable custom motd feature")
    public boolean enabled = false;

    @Setting(value = "line1")
    public String line1 = "&aExample motd";

    @Setting(value = "line2")
    public String line2 = "&bSecond line";
}
