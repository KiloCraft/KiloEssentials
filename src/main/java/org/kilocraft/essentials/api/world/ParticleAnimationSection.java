package org.kilocraft.essentials.api.world;

import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ParticleAnimationSection<P extends ParticleOptions> {
    private final P effect;
    private final boolean longDistance;
    private final RelativePosition relativePosition;
    private final double oX;
    private final double oY;
    private final double oZ;
    private final double speed;
    private final int count;
    private final Type type;
    private final boolean relative;

    public ParticleAnimationSection(P effect,
                                    boolean longDistance,
                                    RelativePosition relPos,
                                    double offsetX, double offsetY, double offsetZ,
                                    double speed, int count, boolean relative) {

        this.effect = effect;
        this.longDistance = longDistance;
        this.relativePosition = relPos;
        this.oX = offsetX;
        this.oY = offsetY;
        this.oZ = offsetZ;
        this.speed = speed;
        this.count = count;
        this.type = Type.NORMAL;
        this.relative = relative;
    }

    @Nullable
    public static ParticleType<?> getEffectByName(String name) {
        for (ResourceLocation id : Registry.PARTICLE_TYPE.keySet()) {
            if (id.getPath().equals(name.toLowerCase()))
                return Registry.PARTICLE_TYPE.get(id);
        }

        return null;
    }

    public ParticleAnimationSection.Type getType() {
        return this.type;
    }

    public boolean getRelative() {
        return this.relative;
    }

    @Nullable
    public ClientboundLevelParticlesPacket toPacket(Vec3 vec3d, double rotation) {
        Vec3 vec = this.relativePosition.getRelativeVector(vec3d);

        if (this.getRelative()) {
            if (rotation < 0) {
                rotation += 360;
            }

            if (rotation > 360) {
                rotation -= 360;
            }

            rotation = Math.toRadians(rotation);

            double x = this.relativePosition.getX() * Math.cos(rotation) - this.relativePosition.getZ() * Math.sin(rotation);
            double z = this.relativePosition.getX() * Math.sin(rotation) + this.relativePosition.getZ() * Math.cos(rotation);
            vec = new Vec3(x + vec3d.x, vec.y, z + vec3d.z);
        }

        return new ClientboundLevelParticlesPacket(
                this.effect,
                this.longDistance,
                vec.x(), vec.y(), vec.z(),
                (float) this.oX, (float) this.oY, (float) this.oZ,
                (float) this.speed, this.count
        );
    }

    public enum Type {
        NORMAL, // Normal Particle Effect
        BLOCK,  // BlockStateParticleEffect
        DUST,   // DustParticleEffect
        ITEM,   // ItemStackParticleEffect
    }

}
