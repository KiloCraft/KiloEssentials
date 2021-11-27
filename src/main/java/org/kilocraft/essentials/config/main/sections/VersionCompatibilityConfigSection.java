package org.kilocraft.essentials.config.main.sections;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public class VersionCompatibilityConfigSection {

    @Setting(value = "enabled")
    public boolean enabled = false;

    @Setting(value = "versionProtocol")
    public int versionProtocol = 757;

    @Setting(value = "versionName")
    public String versionName = "1.18";

}
