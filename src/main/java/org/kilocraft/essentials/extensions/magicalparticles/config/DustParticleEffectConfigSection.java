package org.kilocraft.essentials.extensions.magicalparticles.config;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public class DustParticleEffectConfigSection {

    @Setting("rgb")
    public String rgb = "1 0 0";

    @Setting("scale")
    public float scale = 1.0F;

}
