package org.kilocraft.essentials.extensions.magicalparticles.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.ArrayList;
import java.util.List;

@ConfigSerializable
public class ParticleTypeConfigSection {
    @Setting public List<ParticleFrameConfigSection> frames = new ArrayList<ParticleFrameConfigSection>(){{
        add(new ParticleFrameConfigSection());
    }};
}
