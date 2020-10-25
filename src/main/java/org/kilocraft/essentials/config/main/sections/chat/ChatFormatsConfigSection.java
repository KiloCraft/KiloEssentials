package org.kilocraft.essentials.config.main.sections.chat;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class ChatFormatsConfigSection {

    @Setting("publicChat")
    public String publicChat = "%user_displayname% <gray>» <reset>";

    @Setting("staffChat")
    public String staffChat = "<red>[<dark_red>Staff]<reset> %user_displayname% <gray>» <reset>";

    @Setting("builderChat")
    public String builderChat = "<green>[<dark_green>Builder]<reset> %user_displayname% <gray>» <reset>";

}
