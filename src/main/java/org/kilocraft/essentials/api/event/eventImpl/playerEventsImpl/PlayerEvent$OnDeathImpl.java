package org.kilocraft.essentials.api.event.eventImpl.playerEventsImpl;

import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.api.event.playerEvents.PlayerEvent$OnDeath;

public class PlayerEvent$OnDeathImpl implements PlayerEvent$OnDeath {

    private ServerPlayerEntity player;

    public PlayerEvent$OnDeathImpl(ServerPlayerEntity playerEntity) {
        this.player = playerEntity;
    }

    @Override
    public ServerPlayerEntity getPlayer() {
        return player;
    }
}

