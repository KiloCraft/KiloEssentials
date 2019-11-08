package org.kilocraft.essentials.serverwarp;

import net.minecraft.nbt.CompoundTag;
import org.kilocraft.essentials.config.KiloConifg;

public class Warp {
    private String permissionBaseName = KiloConifg.getProvider().getMain().getValue("warps.permission_prefix");
    private String name;
    private double x, y, z;
    private int dimension;
    private boolean requirePermission;

    private float dX, dY;

    public Warp(String name, double x, double y, double z, float yaw, float pitch, int dimension, boolean requirePermission) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
        this.dimension = dimension;
        this.requirePermission = requirePermission;
        this.dX = pitch;
        this.dY = yaw;
    }

    public Warp(String name, CompoundTag tag) {
        this.name = name;
        fromTag(tag);
    }

    public String getPermissionBaseName() {
        return permissionBaseName;
    }

    public String getName() {
        return this.name;
    }


    public boolean doesRequirePermission() {
        return this.requirePermission;
    }

    public float getPitch() {
        return this.dX;
    }

    public float getYaw() {
        return this.dY;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

    public int getDimension() {
        return this.dimension;
    }


    public String getPermissionNode() {
        return "" + (this.requirePermission ? permissionBaseName + "." + this.name : "");
    }

    public CompoundTag toTag() {
        CompoundTag compoundTag = new CompoundTag();

        {
            CompoundTag pos = new CompoundTag();
            pos.putDouble("x", this.x);
            pos.putDouble("y", this.y);
            pos.putDouble("z", this.z);

            compoundTag.put("pos", pos);
        }
        {
            CompoundTag direction = new CompoundTag();
            direction.putFloat("dX", this.dX);
            direction.putFloat("dY", this.dY);

            compoundTag.put("direction", direction);
        }

        compoundTag.putInt("dimension", this.dimension);
        compoundTag.putBoolean("requires_permission", this.requirePermission);

        return compoundTag;
    }

    public void fromTag(CompoundTag tag) {
        {
            CompoundTag pos = tag.getCompound("pos");
            this.x = pos.getDouble("x");
            this.y = pos.getDouble("y");
            this.z = pos.getDouble("z");
        }
        {
            CompoundTag dir = tag.getCompound("direction");
            this.dimension = tag.getInt("dimension");
            this.dX = dir.getFloat("dX");
            this.dY = dir.getFloat("dY");
        }

        this.requirePermission = tag.getBoolean("requires_permission");
    }
}
