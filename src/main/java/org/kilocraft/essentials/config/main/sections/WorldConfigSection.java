package org.kilocraft.essentials.config.main.sections;

import com.google.common.collect.Lists;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.List;

@ConfigSerializable
public class WorldConfigSection {

    @Setting(value = "allowTheEnd", comment = "If set to false no one will be able to go to The End")
    public boolean allowTheEnd = true;

    @Setting(value = "disabledDimensions", comment = "Put the identifier of a dimension here to disable it!")
    public List<String> disabledDimensions = Lists.newArrayList("myserver:custom_dimension");

    @Setting(value = "kickFromDimensionIfNotAllowed", comment = "If set to true and if a player is inside of a disallowed dimension then they'll get\nteleported back to their Spawn Point")
    public boolean kickFromDimension = true;

    @Setting(value = "kickOutMessage", comment = "Set the message for when a player gets kicked back to their spawn point If the dimension is disallowed, Values: %s")
    public String kickOutMessage = "&cThe %s dimension is disabled!";
}
