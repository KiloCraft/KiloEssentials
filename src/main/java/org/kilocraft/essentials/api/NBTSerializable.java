package org.kilocraft.essentials.api;

import net.minecraft.nbt.CompoundTag;

public interface NBTSerializable {
    CompoundTag toTag();

    void fromTag(CompoundTag tag);
}
