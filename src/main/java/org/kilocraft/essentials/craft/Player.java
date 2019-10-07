package org.kilocraft.essentials.craft;

import io.github.indicode.fabric.worlddata.NBTWorldData;
import net.minecraft.nbt.CompoundTag;
import org.kilocraft.essentials.craft.homesystem.Home;

import java.io.File;
import java.util.List;
import java.util.UUID;

public class Player extends NBTWorldData {
    private UUID uuid;
    private String name;
    private String nickname;
    private List<Home> homes;
    private List<String> properties;
    private String balance;

    public Player() {
    }

    @Override
    public File getSaveFile(File file, File file1, boolean b) {
        return null;
    }

    @Override
    public CompoundTag toNBT(CompoundTag compoundTag) {
        return null;
    }

    @Override
    public void fromNBT(CompoundTag compoundTag) {

    }
}
