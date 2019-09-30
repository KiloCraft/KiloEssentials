package org.kilocraft.essentials.api.event;

import org.kilocraft.essentials.api.event.playerEvents.*;
import org.kilocraft.essentials.api.event.serverEvents.*;

public enum Events {
    PLAYEREVENT_ON_CONNECT("OnConnect", "PlayerEvent", PlayerEvent$OnConnect.class),
    PLAYEREVENT_ON_DISCONNECT("OnDisconnect", "PlayerEvent", PlayerEvent$OnDisconnect.class),
    PLAYEREVENT_ON_PLACE_BLOCK("OnPlaceBlock", "PlayerEvent", PlayerEvent$OnPlaceBlock.class),
    PLAYEREVENT_ON_BREAKING_BLOCK("OnBlockBroken", "PlayerEvent", PlayerEvent$OnBreakingBlockEvent.class),
    SERVEREVENT_ON_READY("OnReady", "ServerEvent", ServerEvent$OnReady.class),
    SERVEREVENT_ON_STOP("OnStop", "ServerEvent", ServerEvent$OnStop.class);

    private String name;
    private String groupName;
    private Class eventClass;

    Events(String name, String group, Class event) {
        this.name = name;
        this.groupName = group;
        this.eventClass = event;
    }

    public String getName() {
        return this.name;
    }

    public String getGroupName() {
        return this.groupName;
    }

    public Class getEventClass() {
        return this.eventClass;
    }
}
