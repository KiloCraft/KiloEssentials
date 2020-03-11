package org.kilocraft.essentials.user.inventory;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.user.inventory.CachedInventory;
import org.kilocraft.essentials.api.user.inventory.UserInventory;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.user.ServerUser;

import java.util.ArrayList;
import java.util.List;

public class ServerUserInventory implements UserInventory {
    private static boolean cachingEnabled = KiloConfig.main().cachedInventoriesSection().enabled;
    private static int cacheSize = KiloConfig.main().cachedInventoriesSection().cacheSize;
    private CachedInventory inv;
    private List<CachedInventory> cached;

    public ServerUserInventory(final ServerUser user, final ServerPlayerEntity player) {
        this.inv = new CachedUserInventory(player.inventory);

        if (cachingEnabled) {
            this.cached = new ArrayList<>();
        }
    }

    public ServerUserInventory(final ServerUser user) {
    }

    @Override
    public CachedInventory getMain() {
        return this.inv;
    }

    @Override
    public void load(PlayerInventory inventory) {
        this.inv = new CachedUserInventory(inventory);
    }

    @Override
    public void clearCache() {
        this.cached.clear();
    }

    @Override
    public void cache(CachedInventory cached) {
        this.cached.add(cached);
    }

    @Override
    public @Nullable CachedInventory getCached(int i) {
        return null;
    }

    @Override
    public CompoundTag serialize(CompoundTag tag) {
        if (!cachingEnabled) {
            return tag;
        }

        ListTag list = new ListTag();
        for (CachedInventory inv : this.cached) {
            ListTag listTag = new ListTag();
            PlayerInventory inventory = new PlayerInventory(null);
            inventory.serialize(listTag);
            list.add(listTag);
        }

        tag.put("cache", list);
        return tag;
    }

    @Override
    public void deserialize(CompoundTag tag) {
//        if (!cachingEnabled) {
//            return;
//        }
//
//        ListTag listTag = tag.getList("cache", cacheSize);
//        this.cached = new ArrayList<>();
//        for (int i = 0; i < cacheSize; i++) {
//            PlayerInventory inv = new PlayerInventory(this.inv.player);
//            inv.deserialize(listTag.getList(i));
//            this.cached.add(inv);
//        }
    }
}
