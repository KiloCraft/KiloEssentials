package org.kilocraft.essentials.config.sections;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class StartupScriptConfigSection {
    @Setting(value = "enabled", comment = "Enable the automatic startup script generation")
    public boolean enabled = true;

    @Setting(value = "fabricLoaderName", comment = "The name of the loader jar")
    public String startupScript_FabricLoaderName = "fabric-server-launch.jar";

    @Setting(value = "linuxScreenMode", comment = "Set this to true if you're using screen on a linux distro")
    public boolean startupScript_linuxScreen = false;

    @Setting(value = "linuxScreenName", comment = "Sets the name of the screen If the above option is enabled")
    public String startupScript_linuxScreenName = "minecraft-server";

    @Setting(value = "scriptName", comment = "The name of the script")
    public String startupScript_scriptName = "start-server";

    @Setting(value = "maximumRam", comment = "Sets the maximum memory size\n" +
            "E.g: '4G' for 4 Gigabytes of memory, '712M' for 712 for 712 Megabytes of memory")
    public String startupScript_maximumRam = "3G";
}
