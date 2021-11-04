package org.kilocraft.essentials.config.main.sections;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public class PermissionRequirementConfigSection {

    @Setting(value = "op")
    public int op = 2;

    @Setting(value = "perm")
    public String permission = "myserver.custom.permission.node";

}
