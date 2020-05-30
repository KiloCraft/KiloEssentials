package org.kilocraft.essentials.config.main.sections;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class PermissionRequirementConfigSection {

    @Setting(value = "op")
    public int op = 2;

    @Setting(value = "perm")
    public String permission = "myserver.custom.permission.node";

}
