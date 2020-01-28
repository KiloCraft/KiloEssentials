package org.kilocraft.essentials.user;

import net.minecraft.entity.player.PlayerEntity;
import org.kilocraft.essentials.api.user.OfflineUser;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class OfflineServerUser extends ServerUser implements OfflineUser {
    public OfflineServerUser(UUID uuid) {
        super(uuid);
    }

    @Override
    public CompletableFuture<PlayerEntity> getPlayer() {

        return null;
    }
}
