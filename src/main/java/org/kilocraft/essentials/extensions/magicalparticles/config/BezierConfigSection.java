package org.kilocraft.essentials.extensions.magicalparticles.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class BezierConfigSection {

    @Setting("points")
    public String points;

    @Setting("controlPoints")
    public String controlPoints;

}
