package org.kilocraft.essentials.craft.worldwarps;

import net.minecraft.util.math.BlockPos;
import org.kilocraft.essentials.craft.config.KiloConifg;

public class Warp {
    private String permissionBaseName = KiloConifg.getWarps().getOrElse("permissionNode_baseName", "warp");
    private String name;
    private BlockPos blockPos;
    private boolean requirePermission;

    public Warp(String name, BlockPos blockPos, boolean requirePermission) {
        this.name = name;
        this.blockPos = blockPos;
        this.requirePermission = requirePermission;
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

    public boolean isRequirePermission() {
        return this.requirePermission;
    }

}
