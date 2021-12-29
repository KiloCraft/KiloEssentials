package org.kilocraft.essentials.config.main.sections;

import com.google.common.collect.Lists;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.List;
import java.util.Map;

@ConfigSerializable
public class WorldConfigSection {

    @Setting(value = "disabledDimensions")
    @Comment("Put the identifier of a dimension here to disable it!")
    public List<String> disabledDimensions = Lists.newArrayList("myserver:custom_dimension");

    @Setting(value = "kickFromDimensionIfNotAllowed")
    @Comment("If set to true and if a player is inside of a disallowed dimension then they'll get kicked back to their spawnpoint")
    public boolean kickFromDimension = true;

    @Setting(value = "disabledCommands")
    @Comment("Put the identifier of a dimension and a list of commands to disable them!")
    public Map<String, List<String>> disabledCommands = Map.of("myserver:custom_dimension", Lists.newArrayList("home", "tpa"));

}
