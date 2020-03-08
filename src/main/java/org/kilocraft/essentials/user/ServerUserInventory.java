package org.kilocraft.essentials.user;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.api.user.UserInventory;
import org.kilocraft.essentials.config.KiloConfig;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ServerUserInventory implements UserInventory {
    private static boolean cachingEnabled = KiloConfig.main().cachedInventoriesSection().enabled;
    private static int cacheSize = KiloConfig.main().cachedInventoriesSection().cacheSize;
    private PlayerInventory inventory;
    private List<PlayerInventory> inventories;

    public ServerUserInventory(final ServerUser user, final ServerPlayerEntity player) {
        this.inventory = new PlayerInventory(player);

        if (cachingEnabled) {
            this.inventories = new ArrayList<>();
        }
    }

    public ServerUserInventory(final ServerUser user) {
    }

    @Override
    public PlayerInventory getMain() {
        return this.inventory;
    }

    @Override
    public void load(PlayerInventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public void clearCache() {
        this.inventories.clear();
    }

    @Override
    public void cache(PlayerInventory inventory) {
        this.inventories.add(inventory);
    }

    @Override
    public PlayerInventory getCached(int i) {
        return this.inventories.get(i);
    }

    @Override
    public CompoundTag serialize(CompoundTag tag) {
        if (!cachingEnabled) {
            return tag;
        }

        ListTag list = new ListTag();
        for (PlayerInventory inv : this.inventories) {
            ListTag listTag = new ListTag();
            inv.serialize(listTag);
            list.add(listTag);
        }

        tag.put("cache", list);
        return tag;
    }

    @Override
    public void deserialize(CompoundTag tag) {
        if (!cachingEnabled) {
            return;
        }

        ListTag listTag = tag.getList("cache", cacheSize);
        this.inventories = new ArrayList<>();
        for (int i = 0; i < cacheSize; i++) {
            PlayerInventory inv = new PlayerInventory(this.inventory.player);
            inv.deserialize(listTag.getList(i));
            this.inventories.add(inv);
        }
    }
}
