package org.kilocraft.essentials.extensions.warps.playerwarps;

import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.api.world.location.Location;
import org.kilocraft.essentials.extensions.warps.Warp;
import org.kilocraft.essentials.util.nbt.NBTUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerWarp extends Warp implements Comparable<PlayerWarp> {
    private UUID owner;
    private String type;
    private String description;

    public PlayerWarp(String name, Location location, UUID owner, String type, String description) {
        super(name, location);
        this.owner = owner;
        this.type = type;
        this.description = description;
    }

    public PlayerWarp(String name, NbtCompound tag) {
        super(name, null);
        this.fromTag(tag);
    }

    public String getType() {
        return this.type;
    }

    public UUID getOwner() {
        return this.owner;
    }

    public String getDescription() {
        return this.description;
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound NbtCompound = super.toTag();
        NbtCompound.putString("type", this.type);
        NbtCompound.putString("desc", this.description);
        NBTUtils.putUUID(NbtCompound, "owner", this.owner);

        return NbtCompound;
    }

    @Override
    public void fromTag(NbtCompound tag) {
        super.fromTag(tag);
        this.type = tag.getString("type");
        this.description = tag.getString("desc");
        this.owner = NBTUtils.getUUID(tag, "owner");
    }

    @Override
    public int compareTo(@NotNull PlayerWarp o) {
        return this.getName().compareTo(o.getName());
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
