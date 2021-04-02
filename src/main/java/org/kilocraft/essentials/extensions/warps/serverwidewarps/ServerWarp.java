package org.kilocraft.essentials.extensions.warps.serverwidewarps;

import net.minecraft.nbt.NbtCompound;
import org.kilocraft.essentials.api.world.location.Location;
import org.kilocraft.essentials.extensions.warps.Warp;

public class ServerWarp extends Warp {
    private boolean addCommand;

    public ServerWarp(String name, Location location, boolean addCommand) {
        super(name, location);
        this.addCommand = addCommand;
    }

    public ServerWarp(String name, NbtCompound tag) {
        super(name, null);
        fromTag(tag);
    }

    public boolean addCommand() {
        return this.addCommand;
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound NbtCompound = super.toTag();

        if (this.addCommand) {
            NbtCompound.putBoolean("addCmd", true);
        }

        return NbtCompound;
    }

    @Override
    public void fromTag(NbtCompound tag) {
        super.fromTag(tag);

        if (tag.contains("addCmd")) {
            this.addCommand = true;
        }
    }
}
