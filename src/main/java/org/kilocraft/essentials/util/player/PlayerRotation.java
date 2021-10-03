package org.kilocraft.essentials.util.player;

import org.kilocraft.essentials.api.util.EntityRotation;

public class PlayerRotation implements EntityRotation {
    private float yaw, pitch;

    public PlayerRotation(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    @Override
    public float getYaw() {
        return this.yaw;
    }

    @Override
    public float getPitch() {
        return this.pitch;
    }

    @Override
    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    @Override
    public void setPitch(float pitch) {
        this.pitch = pitch;
    }
}
