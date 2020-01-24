package org.kilocraft.essentials.api.event.player;

import net.minecraft.server.network.packet.ClientCommandC2SPacket;
import org.kilocraft.essentials.api.event.Cancellable;
import org.kilocraft.essentials.api.event.Event;
import org.kilocraft.essentials.api.event.context.Contextual;
import org.kilocraft.essentials.api.event.context.PlayerContext;

public interface PlayerClientCommandEvent extends Event, PlayerContext, Contextual, Cancellable {
    ClientCommandC2SPacket.Mode getCommandMode();
}
