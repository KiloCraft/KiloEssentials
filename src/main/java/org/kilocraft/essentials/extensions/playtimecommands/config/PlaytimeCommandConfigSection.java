package org.kilocraft.essentials.extensions.playtimecommands.config;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.ArrayList;
import java.util.List;

@ConfigSerializable
public class PlaytimeCommandConfigSection {

    @Setting("secondsRequired")
    public long seconds = 100;

    @Setting("executes")
    public List<String> commands = new ArrayList<String>() {{
        this.add("sudo ${user.name} say I've reached 100 seconds of playtime!");
    }};

}
