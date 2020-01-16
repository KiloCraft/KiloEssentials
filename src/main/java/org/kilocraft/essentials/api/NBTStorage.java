package org.kilocraft.essentials.api;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public interface NBTStorage {

    File getSaveFile();

    CompoundTag serialize();

    void deserialize(@NotNull CompoundTag compoundTag);

}
