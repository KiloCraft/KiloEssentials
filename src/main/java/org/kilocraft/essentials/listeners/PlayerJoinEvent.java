package org.kilocraft.essentials.listeners;

import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.player.PlayerConnectEvent;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.user.ServerUserManager;

public class PlayerJoinEvent implements EventHandler<PlayerConnectEvent> {
    @Override
    public void handle(PlayerConnectEvent event) {
        ((ServerUserManager) KiloServer.getServer().getUserManager()).onJoin(event.getPlayer());
        
        // Accually use the saved data
        ServerPlayerEntity player = event.getPlayer();
        OnlineUser user = KiloServer.getServer().getUserManager().getOnline(player);

        if (user.getLastSavedLocation() == null)
        	user.saveLocation();
    }
}
