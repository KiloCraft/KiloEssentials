package org.kilocraft.essentials.extensions.magicalparticles.config;

import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.Optional;

@ConfigSerializable
public class ParticleFrameConfigSection {

    @Setting("count")
    public int count = 9;

    @Setting("effect")
    public String effect = "dragon_breath";

    @Setting("blockProperties")
    public final BlockStateParticleEffectConfigSection blockStateSection = new BlockStateParticleEffectConfigSection();

    @Setting("dustProperties")
    public final DustParticleEffectConfigSection dustParticleSection = new DustParticleEffectConfigSection();

    @Setting("longDistance")
    public boolean longDistance = true;

    @Setting("pos")
    public String pos = "0 0 0";

    @Setting("offset")
    public String offset = "0 0.5 0";

    @Setting("speed")
    public double speed = 0.0D;

    @Setting("shapeProperties")
    private final ShapeConfigSection shapeParticleSection = this.pos.contains("^") ? new ShapeConfigSection() : null;

    public Optional<BlockStateParticleEffectConfigSection> getBlockStateSection() {
        if (this.isEqualTo(ParticleTypes.BLOCK) || this.isEqualTo(ParticleTypes.ITEM)) {
            return Optional.of(this.blockStateSection);
        } else {
            return Optional.empty();
        }
    }

    public Optional<DustParticleEffectConfigSection> getDustParticleSection() {
        if (this.isEqualTo(ParticleTypes.DUST) || this.isEqualTo(ParticleTypes.FALLING_DUST)) {
            return Optional.of(this.dustParticleSection);
        } else {
            return Optional.empty();
        }
    }

    public Optional<ShapeConfigSection> getShapeSection() {
        return Optional.ofNullable(this.shapeParticleSection);
    }

    private boolean isEqualTo(ParticleType<?> particleType) {
        Identifier identifier = Registry.PARTICLE_TYPE.getId(particleType);
        return identifier != null && this.effect.equalsIgnoreCase(identifier.getPath());
    }
}
