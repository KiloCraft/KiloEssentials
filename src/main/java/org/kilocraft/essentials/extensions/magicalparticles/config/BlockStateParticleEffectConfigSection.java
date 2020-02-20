package org.kilocraft.essentials.extensions.magicalparticles.config;

import net.minecraft.util.registry.Registry;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class BlockStateParticleEffectConfigSection {

    @Setting("id")
    public String blockId = Registry.BLOCK.getDefaultId().toString();

}
