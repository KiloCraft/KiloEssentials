package org.kilocraft.essentials.config.main.sections;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public class PermissionRequirementConfigSection {

    @Setting(value = "op")
    public int op = 0;

    @Setting(value = "perm")
    public String permission = "kiloessentials.magicparticles.permission.node";

}
