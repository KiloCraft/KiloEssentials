package org.kilocraft.essentials.api.world;

import net.minecraft.client.network.packet.ParticleS2CPacket;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

public class ParticleFrame {
    private ParticleEffect effect;
    private boolean longDistance;
    private RelativePosition relativePosition;
    private double oX, oY, oZ;
    private double speed;
    private int count;

    public <P extends ParticleEffect> ParticleFrame(P effect,
                                                  boolean longDistance,
                                                  RelativePosition relPos,
                                                  double offsetX, double offsetY, double offsetZ,
                                                  double speed, int count) {

        this.effect = effect;
        this.longDistance = longDistance;
        this.relativePosition = relPos;
        this.oX = offsetX;
        this.oY = offsetY;
        this.oZ = offsetZ;
        this.speed = speed;
        this.count = count;
    }

    public <P extends ParticleEffect> ParticleFrame(P effect,
                                                    boolean longDistance,
                                                    double offsetX, double offsetY, double offsetZ,
                                                    double speed, int count) {

        this.effect = effect;
        this.longDistance = longDistance;
        this.relativePosition = new RelativePosition(0, 0, 0);
        this.oX = offsetX;
        this.oY = offsetY;
        this.oZ = offsetZ;
        this.speed = speed;
        this.count = count;
    }

    @Nullable
    public static ParticleEffect getEffectByName(String name) {
        for (Identifier id : Registry.PARTICLE_TYPE.getIds()) {
            if (id.getPath().equals(name))
                return (ParticleEffect) Registry.PARTICLE_TYPE.get(id);
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

    public ParticleS2CPacket toPacket(Vec3d vec3d) {
        Vec3d vec = relativePosition.getRelativeVector(vec3d);
        return new ParticleS2CPacket(effect, longDistance, vec.getX(), vec.getY(), vec.getZ(), (float) oX, (float) oY, (float) oZ, (float) speed, count);
    }
}
