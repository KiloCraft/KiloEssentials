package org.kilocraft.essentials.extensions.customcommands.config.sections;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class CCReqConfigSection {

    @Setting(value = "op")
    public int op = 2;

    @Setting(value = "perm")
    public String permission = "myserver.custom.permission.node";

}
