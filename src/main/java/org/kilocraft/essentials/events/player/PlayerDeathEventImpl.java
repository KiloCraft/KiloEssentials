package org.kilocraft.essentials.events.player;

import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.event.player.PlayerDeathEvent;
import org.kilocraft.essentials.api.user.OnlineUser;

public class PlayerDeathEventImpl implements PlayerDeathEvent {

    private ServerPlayerEntity player;
    private boolean cancelled;

    public PlayerDeathEventImpl(ServerPlayerEntity playerEntity) {
        this.player = playerEntity;
    }

    @Override
    public ServerPlayerEntity getPlayer() {
        return player;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean set) {
        this.cancelled = set;
    }

    @Override
    public OnlineUser getUser() {
        return KiloServer.getServer().getOnlineUser(this.player);
    }
}

