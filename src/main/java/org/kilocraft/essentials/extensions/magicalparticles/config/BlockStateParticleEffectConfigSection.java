package org.kilocraft.essentials.extensions.magicalparticles.config;

import net.minecraft.core.Registry;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public class BlockStateParticleEffectConfigSection {

    @Setting("id")
    public String blockId = Registry.BLOCK.getDefaultKey().toString();

}
