package org.kilocraft.essentials.config.main.sections.chat;

import com.google.common.collect.Lists;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.List;

@ConfigSerializable
public class SocialSpyConfigSection {

    @Setting(value = "prefix", comment = "Set the format of the social spy warning")
    public String prefix = "&8[&5Spy&8] &7[&c%SOURCE%&r&3 -> &7%TARGET%&r&7]&f";

    @Setting(value = "sensitiveWords", comment = "The words that the social spy is sensitive to")
    public List<String> sensitiveWords = Lists.newArrayList("server", "");

    @Setting(value = "sensitiveToUrls", comment = "Enable sensitivity to links")
    public boolean sensitiveToUrls = true;

}
