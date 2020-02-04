package org.kilocraft.essentials.config.sections;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class WorldConfigSection {

    @Setting(value = "allowTheEnd", comment = "If set to false no one will be able to go to The End")
    public boolean allowTheEnd = true;

    @Setting(value = "allowTheNether", comment = "If set to false no one will be able to go to The Nether")
    public boolean allowTheNether = true;

    @Setting(value = "allowTheEnd", comment = "If set to true and if a player is inside of a disallowed dimension then they'll get kicked back to their spawnpoint")
    public boolean kickFromDimension = true;
}
