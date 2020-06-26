package org.kilocraft.essentials.config.main.sections;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.ArrayList;
import java.util.List;

@ConfigSerializable
public class VotifierConfigSection {

    @Setting(value = "enabled")
    public boolean enabled = false;

    @Setting(value = "port", comment = "Votifier port")
    public int port = 8192;

    @Setting(value = "commands", comment = "Commands, which get executed, when a vote is received, Variables: %PLAYER%, %SERVICE%, %TIMESTAMP and %ADDRESS%")
    public List<String> commands = new ArrayList<String>(){ {
        this.add("tellraw @a [{\"text\":\"&a%PLAYER% voted on %SERVICE%\"}]");
    }
    };
}
