package org.kilocraft.essentials.api.user.inventory;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;

public interface CachedInventory {

    Inventory get();

    void paste(Inventory inventory);

}
