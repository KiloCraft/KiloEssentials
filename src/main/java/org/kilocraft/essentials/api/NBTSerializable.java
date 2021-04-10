package org.kilocraft.essentials.api;

import net.minecraft.nbt.NbtCompound;

public interface NBTSerializable {
    /**
     * Convert all the data into a NbtCompound for serialization
     *
     * @return The Tag to Save
     */
    NbtCompound toTag();

    /**
     * Get all the data from the NbtCompound for deserialization
     *
     * @param tag Tag to get the data from
     */
    void fromTag(NbtCompound tag);
}
