package org.kilocraft.essentials.config.main.sections;

import com.google.common.collect.Lists;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.List;

@ConfigSerializable
public class VotifierConfigSection {

    @Setting(value = "port", comment = "Votifier port")
    public int port = 8192;

    @Setting(value = "commands", comment = "Commands, which get executed, when a vote is received, Variables: %PLAYER%, %SERVICE%, %TIMESTAMP and %ADDRESS%")
    public List<String> commands = Lists.newArrayList("tellraw @a [{\"text\":\"&a%PLAYER% voted on %SERVICE%\"}]");
}