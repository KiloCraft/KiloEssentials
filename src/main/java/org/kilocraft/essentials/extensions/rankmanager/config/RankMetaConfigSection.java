package org.kilocraft.essentials.extensions.rankmanager.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class RankMetaConfigSection {

    @Setting
    public int weight = 1;

    @Setting
    public String prefix = "";

    @Setting
    public String suffix = "";

}
