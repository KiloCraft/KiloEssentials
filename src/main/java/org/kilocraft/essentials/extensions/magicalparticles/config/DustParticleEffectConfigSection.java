package org.kilocraft.essentials.extensions.magicalparticles.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.Arrays;
import java.util.List;

@ConfigSerializable
public class DustParticleEffectConfigSection {

    @Setting("rgb")
    public String rgb = "1 0 0";

    @Setting("scale")
    public float scale = 1.0F;

}
