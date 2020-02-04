package org.kilocraft.essentials.config.sections.chat;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class PrivateChatConfigSection {

    @Setting(value = "format")
    public String privateChat = "&7[&3%SOURCE%&r&7 -> &7%TARGET%&r&7]&f %MESSAGE%";

    @Setting(value = "meFormat")
    public String privateChatMeFormat = "&cme";
}
