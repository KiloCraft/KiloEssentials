package org.kilocraft.essentials.extensions.playtimecommands.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.ArrayList;
import java.util.List;

@ConfigSerializable
public class PlaytimeCommandConfigSection {

    @Setting("secondsRequired")
    public long seconds = 100;

    @Setting("executes")
    public List<String> commands = new ArrayList<String>(){{
        this.add("give ${user.name} apple 2");
        this.add("sudo ${user.name} say I've reached 100 seconds of playtime!");
    }};

}
