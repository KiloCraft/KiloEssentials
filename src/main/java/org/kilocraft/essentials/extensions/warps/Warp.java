package org.kilocraft.essentials.extensions.warps;

import net.minecraft.nbt.CompoundTag;
import org.kilocraft.essentials.util.Location;

public class Warp {
    private String name;
    private Location location;
    private boolean addCommand;

    public Warp(String name, Location location, boolean addCommand) {
        this.name = name;
        this.location = location;
        this.addCommand = addCommand;
    }

    public Warp(String name, CompoundTag tag) {
        this.name = name;
        fromTag(tag);
    }

    public String getName() {
        return this.name;
    }

    public Location getLocation() {
        return this.location;
    }

    public boolean getAddCommand() {
        return this.addCommand;
    }


    public CompoundTag toTag() {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.put("loc", this.location.toTag());

        if (this.addCommand)
            compoundTag.putBoolean("addCmd", true);

        return compoundTag;
    }

    public void fromTag(CompoundTag tag) {
        this.location.fromTag(tag);

        if (tag.contains("addCmd"))
            this.addCommand = true;

    }
}
