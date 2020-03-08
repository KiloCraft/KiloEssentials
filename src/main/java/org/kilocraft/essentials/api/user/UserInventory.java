package org.kilocraft.essentials.api.user;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

public interface UserInventory {

    @Nullable
    PlayerInventory getMain();

    void load(PlayerInventory inventory);

    void clearCache();

    void cache(PlayerInventory inventory);

    @Nullable
    PlayerInventory getCached(int i);

    CompoundTag serialize(CompoundTag tag);

    void deserialize(CompoundTag tag);

}
