package org.kilocraft.essentials.events.player;

import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.event.player.PlayerClientCommandEvent;
import org.kilocraft.essentials.api.user.OnlineUser;

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

    @Override
    public OnlineUser getUser() {
        return KiloServer.getServer().getOnlineUser(this.player);
    }
}
