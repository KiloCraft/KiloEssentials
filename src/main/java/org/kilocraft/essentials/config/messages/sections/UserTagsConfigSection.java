package org.kilocraft.essentials.config.messages.sections;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class UserTagsConfigSection {

    @Setting(value = "online", comment = "User's name tag when they are online")
    public String online = "%DISPLAYNAME%";

    @Setting(value = "offline", comment = "User's name tag when they are offline")
    public String offline = "{DISPLAYNAME} &7(&cOffline&7)";

}
