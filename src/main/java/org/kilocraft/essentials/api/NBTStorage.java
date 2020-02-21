package org.kilocraft.essentials.api;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.provided.KiloFile;

public interface NBTStorage {

    KiloFile getSaveFile();

    CompoundTag serialize();

    void deserialize(@NotNull CompoundTag compoundTag);

}
