package org.kilocraft.essentials.api.world;

import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

public class ParticleAnimationSection<P extends ParticleEffect> {
    private P effect;
    private boolean longDistance;
    private RelativePosition relativePosition;
    private double oX, oY, oZ;
    private double speed;
    private int count;
    private Type type;
    private boolean relative;

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
        for (Identifier id : Registry.PARTICLE_TYPE.getIds()) {
            if (id.getPath().equals(name.toLowerCase()))
                return Registry.PARTICLE_TYPE.get(id);
        }

        return null;
    }

    public ParticleEffect getParticleEffect() {
        return effect;
    }

    public boolean isLongDistance() {
        return longDistance;
    }

    public RelativePosition getRelativePosition() {
        return relativePosition;
    }

    public double getOffsetX() {
        return oX;
    }

    public double getOffsetY() {
        return oY;
    }

    public double getOffsetZ() {
        return oZ;
    }

    public double getSpeed() {
        return speed;
    }

    public int getCount() {
        return count;
    }

    public ParticleAnimationSection.Type getType() {
        return type;
    }

    public P getParticleType() {
        return effect;
    }

    public boolean getRelative () {
        return relative;
    }

    @Nullable
    public ParticleS2CPacket toPacket(Vec3d vec3d, double rotation) {
        Vec3d vec = relativePosition.getRelativeVector(vec3d);

        if (getRelative()) {
            if (rotation < 0) {
                rotation += 360;
            }

            if (rotation > 360) {
                rotation -= 360;
            }

            rotation = Math.toRadians(rotation);

            double x = relativePosition.getX() * Math.cos(rotation) - relativePosition.getZ() * Math.sin(rotation);
            double z = relativePosition.getX() * Math.sin(rotation) + relativePosition.getZ() * Math.cos(rotation);
            vec = new Vec3d(x + vec3d.x, vec.y, z + vec3d.z);
        }

        return new ParticleS2CPacket(
                this.effect,
                longDistance,
                vec.getX(), vec.getY(), vec.getZ(),
                (float) oX, (float) oY, (float) oZ,
                (float) speed, count
        );
    }

    public enum Type {
        NORMAL, // Normal Particle Effect
        BLOCK,  // BlockStateParticleEffect
        DUST,   // DustParticleEffect
        ITEM,   // ItemStackParticleEffect
    }

}
