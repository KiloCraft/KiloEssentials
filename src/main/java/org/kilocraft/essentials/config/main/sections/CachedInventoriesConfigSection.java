package org.kilocraft.essentials.config.main.sections;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class CachedInventoriesConfigSection {

    @Setting(value = "enabled", comment = "Enable this Feature")
    public boolean enabled = true;

    @Setting(value = "cacheSize", comment = "The amount of inventories to keep")
    public int cacheSize = 3;

    @Setting(value = "saveOnDeath", comment = "Saves the Inventories on death")
    public boolean saveOnDeath = true;

}
