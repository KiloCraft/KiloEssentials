package org.kilocraft.essentials.config.sections.chat;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class ChatPingSoundConfigSection {

    @Setting(value = "enabled", comment = "Enable or disable the Chat Ping Sound")
    public boolean enabled = true;

    @Setting(value = "id", comment = "Sound identifier, you can search for them through the '/playsound' command in the game!")
    public String id = "entity.experience_orb.pickup";

    @Setting(value = "volume", comment = "The volume of the sound, Can be between 0 and 3")
    public double volume = 3.0D;

    @Setting(value = "pitch", comment = "Pitch of the sound, can be between 0 and 3, Default: 1.0")
    public double pitch = 1.0D;

}
