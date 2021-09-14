package org.kilocraft.essentials.config.messages.sections;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class GeneralConfigSection {

    @Setting(value = "prefix")
    public String prefix = "&bKiloEssentials &8>&7>";

    @Setting(value = "infoPrefix")
    public String infoPrefix = "&7[&6 i &7]&7";

    @Setting(value = "errorPrefix")
    public String errorPrefix = "&7[&c ! &7]&7";

    @Setting(value = "userTag")
    private final UserTagsConfigSection userTagSection = new UserTagsConfigSection();

    public UserTagsConfigSection userTags() {
        return this.userTagSection;
    }
}
