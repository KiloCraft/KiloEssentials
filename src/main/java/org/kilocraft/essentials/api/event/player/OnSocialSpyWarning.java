package org.kilocraft.essentials.api.event.player;

import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.event.Event;
import org.kilocraft.essentials.api.user.OnlineUser;

import java.util.List;

public interface OnSocialSpyWarning extends Event {
    String getMessage();

    List<String> getMarked();

    ServerCommandSource getSource();

    OnlineUser getReceiver();
}
