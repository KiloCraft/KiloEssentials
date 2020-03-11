package org.kilocraft.essentials.api.user.inventory;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

public interface UserInventory {

    @Nullable
    CachedInventory getMain();

    void load(PlayerInventory inventory);

    void clearCache();

    void cache(CachedInventory cached);

    @Nullable
    CachedInventory getCached(int i);

    CompoundTag serialize(CompoundTag tag);

    void deserialize(CompoundTag tag);

}
