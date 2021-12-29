package org.kilocraft.essentials.api.user;

import net.minecraft.world.inventory.PlayerEnderChestContainer;
import org.jetbrains.annotations.Nullable;

public interface UserPlayer {

    @Nullable
    PlayerEnderChestContainer getEnderchestInventory();

}
