package org.kilocraft.essentials.listeners;

import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.player.PlayerConnectEvent;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.commands.teleport.BackCommand;
import org.kilocraft.essentials.user.ServerUserManager;

import net.minecraft.client.util.math.Vector3f;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.dimension.DimensionType;

public class PlayerJoinEvent implements EventHandler<PlayerConnectEvent> {
    @Override
    public void handle(PlayerConnectEvent event) {
        ((ServerUserManager) KiloServer.getServer().getUserManager()).onJoin(event.getPlayer());
        
        // Accually use the saved data
        ServerPlayerEntity player = event.getPlayer();
        OnlineUser user = KiloServer.getServer().getUserManager().getOnline(player);
        
        if (user.getBackPos() != null) {
        	BackCommand.backLocations.put(player, new Vector3f(user.getBackPos()));
        	BackCommand.backDimensions.put(player, DimensionType.byId(user.getBackDimId()));
        }
    }
}
