package org.kilocraft.essentials.craft.warps;

import net.minecraft.util.math.BlockPos;

public class Warp {
    private String name;
    private BlockPos blockPos;
    private String permission_node;

    public Warp(String name, BlockPos blockPos, String permission) {
        this.name = name;
        this.blockPos = blockPos;
        this.permission_node = permission;
    }
}
