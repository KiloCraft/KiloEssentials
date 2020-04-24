package org.kilocraft.essentials.config.main.sections.chat;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class ChatPrefixesConigSection {

    @Setting(value = "publicChat")
    public String publicChat = "&r%USER_RANKED_DISPLAYNAME% &8>>&r";

    @Setting(value = "staffChat")
    public String staffChat = "&4[&cStaff&4]&r &r%USER_RANKED_DISPLAYNAME% &8>>&r";

    @Setting(value = "builderChat")
    public String builderChat = "&2[&aBuilder&2]&r &r%USER_RANKED_DISPLAYNAME% &8>>&r";

}
