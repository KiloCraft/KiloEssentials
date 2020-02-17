package org.kilocraft.essentials.extensions.magicalparticles.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class ParticleFrameConfigSection {

    @Setting("count")
    public int count = 0;

    @Setting("effect")
    public String effect = "dragon_breath";

    @Setting("longDistance")
    public boolean longDistance = true;

    @Setting("pos")
    public String pos = "0 0 0";

    @Setting("offset")
    public String offset = "10 0.5 0";

    @Setting("speed")
    public double speed = 0.3D;

}
