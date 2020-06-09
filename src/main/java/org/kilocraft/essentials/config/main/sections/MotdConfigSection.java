package org.kilocraft.essentials.config.main.sections;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class MotdConfigSection {

    @Setting(value = "enabled", comment = "Enable custom motd feature")
    public boolean enabled = false;

    @Setting(value = "line1")
    public String line1 = "&aExample motd";

    @Setting(value = "line2")
    public String line2 = "&bSecond line";

    @Setting(value = "rainbow", comment = "Ignore color values and format rainbow")
    public boolean rainbow = false;

    @Setting(value = "offset", comment = "Rainbow color start offset (accepted values: 0.0 - 1.0)")
    public int offset = 0;

    @Setting(value = "ignorespaces")
    public boolean ignorespaces = true;
}
