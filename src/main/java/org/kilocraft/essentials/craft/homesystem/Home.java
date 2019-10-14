package org.kilocraft.essentials.craft.homesystem;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;

public class Home {
    private String owner_uuid;
    private String name;
    private BlockPos blockPos;
    private float dX, dY;

    public Home(String uuid, String name, BlockPos blockPos, double yaw, double pitch) {
        this.owner_uuid = uuid;
        this.name = name;
        this.blockPos = blockPos;
    }

    Home(CompoundTag compoundTag) {
        fromTag(compoundTag);
    }

    public CompoundTag toTag() {
        CompoundTag compoundTag = new CompoundTag();
        CompoundTag homeTag = new CompoundTag();
        {
            CompoundTag pos = new CompoundTag();
            pos.putInt("x", blockPos.getX());
            pos.putInt("y", blockPos.getY());
            pos.putInt("z", blockPos.getZ());

            homeTag.put("pos", pos);
        }
        {
            CompoundTag dir = new CompoundTag();
            dir.putDouble("pitch", dX);
            dir.putDouble("yaw", dY);
            homeTag.put("dir", dir);
        }

        compoundTag.put(name, homeTag);

        return compoundTag;
    }

    public void fromTag(CompoundTag compoundTag) {
        {
            CompoundTag pos = compoundTag.getCompound("pos");
            this.blockPos = new BlockPos(
                    pos.getInt("x"),
                    pos.getInt("y"),
                    pos.getInt("z")
            );
        }
        {
            CompoundTag dir = compoundTag.getCompound("dir");
            this.dX = dir.getFloat("pitch");
            this.dY = dir.getFloat("yaw");
        }
    }

    public String getOwner() {
        return owner_uuid;
    }

    public void setOwner(String uuid) {
        this.owner_uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public void setBlockPos(BlockPos blockPos) {
        this.blockPos = blockPos;
    }

    public float getPitch() {
        return dX;
    }

    public void setPitch(float dX) {
        this.dX = dX;
    }

    public float getYaw() {
        return dY;
    }

    public void setYaw(float dY) {
        this.dY = dY;
    }

}
