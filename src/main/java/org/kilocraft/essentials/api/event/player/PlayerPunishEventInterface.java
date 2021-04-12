package org.kilocraft.essentials.api.event.player;

import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.api.util.EntityIdentifiable;

public interface PlayerPunishEventInterface {

    CommandSourceUser getSource();
    EntityIdentifiable getVictim();
    String getReason();
    long getExpiry();
    boolean isSilent();
    boolean isPermanent();

}
