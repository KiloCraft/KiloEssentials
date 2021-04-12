package org.kilocraft.essentials.api.event.player;

import org.kilocraft.essentials.api.event.Event;
import org.kilocraft.essentials.api.event.context.Contextual;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.api.util.EntityIdentifiable;

public interface PlayerBannedEvent extends PlayerPunishEventInterface, Event, Contextual {

    boolean isIpBan();

}
