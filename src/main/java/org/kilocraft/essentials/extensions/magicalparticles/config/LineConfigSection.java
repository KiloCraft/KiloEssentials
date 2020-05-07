package org.kilocraft.essentials.extensions.magicalparticles.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class LineConfigSection {

    @Setting("startPosition")
    public String startPosition;

    @Setting("endPosition")
    public String endPosition;

}
