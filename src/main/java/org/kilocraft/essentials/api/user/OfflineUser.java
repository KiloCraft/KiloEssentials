package org.kilocraft.essentials.api.user;

import net.minecraft.entity.player.PlayerEntity;

import java.util.concurrent.CompletableFuture;

public interface OfflineUser extends User {
    CompletableFuture<PlayerEntity> getPlayer();
}
