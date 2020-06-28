package org.kilocraft.essentials.api.user;

import net.minecraft.inventory.EnderChestInventory;
import org.jetbrains.annotations.Nullable;

public interface UserPlayer {

    @Nullable
    EnderChestInventory getEnderchestInventory();

}
