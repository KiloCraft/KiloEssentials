package org.kilocraft.essentials.craft.homesystem;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.math.BlockPos;

import java.util.Iterator;
import java.util.List;

public class Home {
    public String owner_uuid;
    public String name;
    public BlockPos blockPos;
    public double dX, dZ;
    public List<Home> homes;

    Home() {
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
            dir.putDouble("dX", dX);
            dir.putDouble("dZ", dZ);
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
            this.dX = dir.getDouble("dX");
            this.dZ = dir.getDouble("dZ");
        }

        ListTag homesTag = (ListTag) compoundTag.getTag(owner_uuid);


        for (Iterator i = homesTag.iterator(); i.hasNext();) {

            //HERE

        }
    }

}
