package org.kilocraft.essentials.extensions.magicalparticles.config;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public class BezierConfigSection {

    @Setting("points")
    public String points;

    @Setting("controlPoints")
    public String controlPoints;

}
