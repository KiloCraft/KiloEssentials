package org.kilocraft.essentials.extensions.warps.serverwidewarps;

import net.minecraft.nbt.CompoundTag;
import org.kilocraft.essentials.api.world.location.Location;
import org.kilocraft.essentials.api.world.location.Vec3dLocation;
import org.kilocraft.essentials.extensions.warps.Warp;

public class ServerWarp extends Warp {
    private boolean addCommand;

    public ServerWarp(String name, Location location, boolean addCommand) {
        super(name, location);
        this.addCommand = addCommand;
    }

    public ServerWarp(String name, CompoundTag tag) {
        super(name, null);
        fromTag(tag);
    }

    public boolean addCommand() {
        return this.addCommand;
    }

    public CompoundTag toTag() {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.put("loc", super.getLocation().toTag());

        if (this.addCommand)
            compoundTag.putBoolean("addCmd", true);

        return compoundTag;
    }

    public void fromTag(CompoundTag tag) {
        if (super.getLocation() == null)
            super.setLocation(Vec3dLocation.dummy());

        super.getLocation().fromTag(tag.getCompound("loc"));

        if (tag.contains("addCmd"))
            this.addCommand = true;

    }
}
