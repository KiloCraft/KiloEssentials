package org.kilocraft.essentials.events.player;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.packet.ClientCommandC2SPacket;
import org.kilocraft.essentials.api.event.player.PlayerClientCommandEvent;

public class PlayerClientCommandEventImpl implements PlayerClientCommandEvent {
    private boolean cancelled = false;
    private ServerPlayerEntity player;
    private ClientCommandC2SPacket.Mode mode;

    public PlayerClientCommandEventImpl(ServerPlayerEntity player, ClientCommandC2SPacket.Mode mode) {
        this.player = player;
        this.mode = mode;
    }

    @Override
    public ClientCommandC2SPacket.Mode getCommandMode() {
        return mode;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean isCancelled) {
        this.cancelled = isCancelled;
    }

    @Override
    public ServerPlayerEntity getPlayer() {
        return player;
    }
}
