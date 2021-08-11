package org.kilocraft.essentials.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.api.util.EntityIdentifiable;

public class PunishEvents {

    public static final Event<MuteEvent> MUTE = EventFactory.createArrayBacked(MuteEvent.class, (callbacks) -> (source, victim, reason, expiry, silent) -> {
        for (MuteEvent callback : callbacks) {
            callback.onMute(source, victim, reason, expiry, silent);
        }
    });

    public interface MuteEvent {
        void onMute(CommandSourceUser source, EntityIdentifiable victim, String reason, long expiry, boolean silent);
    }

    public static final Event<BanEvent> BAN = EventFactory.createArrayBacked(BanEvent.class, (callbacks) -> (source, victim, reason, ipBan, expiry, silent) -> {
        for (BanEvent callback : callbacks) {
            callback.onBan(source, victim, reason, ipBan, expiry, silent);
        }
    });

    public interface BanEvent {
        void onBan(CommandSourceUser source, EntityIdentifiable victim, String reason, boolean ipBan, long expiry, boolean silent);
    }

}
