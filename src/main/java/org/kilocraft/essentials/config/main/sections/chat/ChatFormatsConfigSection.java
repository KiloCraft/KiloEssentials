package org.kilocraft.essentials.config.main.sections.chat;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public class ChatFormatsConfigSection {

    @Setting("publicChat")
    public String publicChat = "%user_displayname% <gray>» <reset>";

    @Setting("staffChat")
    public String staffChat = "<red>[<dark_red>Staff<red>]<reset> %user_displayname% <gray>» <reset>";

    @Setting("builderChat")
    public String builderChat = "<green>[<dark_green>Builder]<reset> %user_displayname% <gray>» <reset>";

}
