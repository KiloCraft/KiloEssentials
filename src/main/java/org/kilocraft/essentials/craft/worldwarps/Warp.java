package org.kilocraft.essentials.craft.worldwarps;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import org.kilocraft.essentials.craft.config.KiloConifg;

public class Warp {
    private String permissionBaseName = KiloConifg.getWarps().getOrElse("permissionNode_baseName", "warp");
    private String name;
    private BlockPos blockPos;
    private boolean requirePermission;
    private double pitch, yaw;

    public Warp(String name, BlockPos blockPos, double pitch, double yaw , boolean requirePermission) {
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

    public CompoundTag toTag() {
        CompoundTag compoundTag = new CompoundTag();
        CompoundTag list = new CompoundTag();
        CompoundTag warpTag = new CompoundTag();
        CompoundTag direction = new CompoundTag();
        {
            CompoundTag pos = new CompoundTag();
            pos.putInt("x", this.blockPos.getX());
            pos.putInt("y", this.blockPos.getY());
            pos.putInt("z", this.blockPos.getZ());

            warpTag.put("pos", pos);
        }
        {
            direction.putDouble("pitch", this.pitch);
            direction.putDouble("yaw", this.yaw);
        }
        
        warpTag.put("direction", direction);
        warpTag.putBoolean("requires_permission", this.requirePermission);
        compoundTag.put(this.name, warpTag);

        return compoundTag;
    }

    public void fromTag(CompoundTag tag) {
        {
            CompoundTag pos = tag.getCompound("pos");
            this.blockPos = new BlockPos(
                    pos.getInt("x"),
                    pos.getInt("y"),
                    pos.getInt("z")
            );
        }
        {
            CompoundTag dir = tag.getCompound("direction");
            this.pitch = dir.getDouble("pitch");
            this.yaw = dir.getDouble("yaw");
        }

        this.requirePermission = tag.getBoolean("requires_permission");
    }

}
