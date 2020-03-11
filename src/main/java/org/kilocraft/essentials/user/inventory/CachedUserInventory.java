package org.kilocraft.essentials.user.inventory;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import org.kilocraft.essentials.api.user.inventory.CachedInventory;

public class CachedUserInventory implements CachedInventory {
    private Inventory inventory;

    public CachedUserInventory(PlayerInventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory get() {
        return inventory;
    }

    @Override
    public void paste(Inventory inventory) {
        this.inventory = inventory;
    }
}
