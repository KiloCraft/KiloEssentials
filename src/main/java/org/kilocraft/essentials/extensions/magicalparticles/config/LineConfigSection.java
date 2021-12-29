package org.kilocraft.essentials.extensions.magicalparticles.config;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public class LineConfigSection {

    @Setting("startPosition")
    public String startPosition;

    @Setting("endPosition")
    public String endPosition;

}
