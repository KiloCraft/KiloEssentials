package org.kilocraft.essentials.craft.worldwarps;

import net.minecraft.util.math.BlockPos;
import org.kilocraft.essentials.craft.config.KiloConifg;

public class Warp {
    private String permissionBaseName = KiloConifg.getMain().getOrElse("warps.permission_prefix", "warp");
    private String name;
    private BlockPos blockPos;
    private boolean requirePermission;

    private float pitch, yaw;

    public Warp(String name, BlockPos blockPos, float pitch, float yaw , boolean requirePermission) {
        this.name = name;
        this.blockPos = blockPos;
        this.requirePermission = requirePermission;
        this.pitch = pitch;
        this.yaw = yaw;
    }

    public String getPermissionBaseName() {
        return permissionBaseName;
    }

    public String getName() {
        return this.name;
    }

    public BlockPos getBlockPos() {
        return this.blockPos;
    }

    public boolean doesRequirePermission() {
        return this.requirePermission;
    }

    public float getPitch() {
        return pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public String getPermissionNode() {
        return "" + (this.requirePermission ? permissionBaseName + "." + this.name : "");
    }

}
