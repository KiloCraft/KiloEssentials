package org.kilocraft.essentials.config.main.sections;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class MiscellaneousConfigSection {

    @Setting(value = "SoundOnHandSwing", comment = "Plays the \"no damage attack sound\" whenever you swing your hand!")
    public boolean handSwingSound = true;
}
