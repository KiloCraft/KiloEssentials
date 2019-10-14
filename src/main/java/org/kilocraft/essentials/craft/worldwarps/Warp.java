package org.kilocraft.essentials.craft.worldwarps;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.dimension.Dimension;
import org.kilocraft.essentials.craft.config.KiloConifg;

public class Warp {
    private String permissionBaseName = KiloConifg.getMain().getOrElse("warps.permission_prefix", "warp");
    private String name;
    private double x, y, z;
    private Dimension dimension;
    private boolean requirePermission;

    private float pitch, yaw;

    public Warp(String name, double x, double y, double z, float pitch, float yaw, Dimension dimension , boolean requirePermission) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
        this.dimension = dimension;
        this.requirePermission = requirePermission;
        this.pitch = pitch;
        this.yaw = yaw;
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
        return this.pitch;
    }

    public float getYaw() {
        return this.yaw;
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

    public Dimension getDimension() {
        return this.dimension;
    }


    public String getPermissionNode() {
        return "" + (this.requirePermission ? permissionBaseName + "." + this.name : "");
    }
    public CompoundTag toTag() {
        CompoundTag compoundTag = new CompoundTag();
        CompoundTag list = new CompoundTag();
        CompoundTag warpTag = new CompoundTag();
        CompoundTag direction = new CompoundTag();
        {
            CompoundTag pos = new CompoundTag();
            pos.putDouble("x", this.x);
            pos.putDouble("y", this.y);
            pos.putDouble("z", this.z);

            warpTag.put("pos", pos);
        }
        {
            direction.putFloat("pitch", this.pitch);
            direction.putFloat("yaw", this.yaw);
        }

        warpTag.put("direction", direction);
        warpTag.putBoolean("requires_permission", this.requirePermission);
        list.put(this.name, warpTag);
        compoundTag.put("warps", list);

        return compoundTag;
    }

    public void fromTag(CompoundTag tag) {
        {
            CompoundTag pos = tag.getCompound("pos");
            this.x = pos.getInt("x");
            this.y = pos.getInt("y");
            this.z = pos.getInt("z");
        }
        {
            CompoundTag dir = tag.getCompound("direction");
            this.pitch = dir.getFloat("pitch");
            this.yaw = dir.getFloat("yaw");
        }

        this.requirePermission = tag.getBoolean("requires_permission");
    }
}
