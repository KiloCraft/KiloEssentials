package org.kilocraft.essentials.extensions.warps.playerwarps;

import net.minecraft.nbt.CompoundTag;
import org.kilocraft.essentials.api.NBTSerializable;
import org.kilocraft.essentials.api.world.location.Location;
import org.kilocraft.essentials.api.world.location.Vec3dLocation;
import org.kilocraft.essentials.extensions.warps.Warp;
import org.kilocraft.essentials.util.NBTUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerWarp extends Warp {
    private UUID owner;
    private String type;

    public PlayerWarp(String name, Location location, UUID owner, String type) {
        super(name, location);
        this.owner = owner;
        this.type = type;
    }

    public PlayerWarp(String name, CompoundTag tag) {
        super(name, null);
        this.fromTag(tag);
    }

    public UUID getOwner() {
        return this.owner;
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag compoundTag = super.toTag();
        compoundTag.putString("type", this.type);
        NBTUtils.putUUID(compoundTag, "owner", this.owner);

        return compoundTag;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        super.fromTag(tag);
        this.type = tag.getString("type");
        this.owner = NBTUtils.getUUID(tag, "owner");
    }

    public static class Type {
        private static List<String> types = new ArrayList<>();
        private String name;

        public Type(String name) {
            this.name = name;
            types.add(name);
        }

        public static void add(String type) {
            types.add(type);
        }

        public String getName() {
            return this.name;
        }

        public static List<String> getTypes() {
            return types;
        }

        public static boolean isValid(String name) {
            return types.contains(name);
        }
    }
}
