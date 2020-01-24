package org.kilocraft.essentials.listeners;

import net.minecraft.server.network.packet.ClientCommandC2SPacket;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.player.PlayerClientCommandEvent;
import org.kilocraft.essentials.extensions.betterchairs.PlayerSitManager;

public class OnClientCommand implements EventHandler<PlayerClientCommandEvent> {
    @Override
    public void handle(PlayerClientCommandEvent event) {
        if (event.getCommandMode().equals(ClientCommandC2SPacket.Mode.STOP_RIDING_JUMP) ) {
            System.out.println("CLIENT COMMAND");
            PlayerSitManager.INSTANCE.sitOff(event.getPlayer());
        }

    }
}
