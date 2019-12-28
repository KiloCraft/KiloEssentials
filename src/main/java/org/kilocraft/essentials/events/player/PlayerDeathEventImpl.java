package org.kilocraft.essentials.events.player;

import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.api.event.player.PlayerDeathEvent;

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
}

