package org.kilocraft.essentials.config.messages.sections;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class GeneralConfigSection {

    @Setting(value = "userTag")
    private final UserTagsConfigSection userTagSection = new UserTagsConfigSection();

    public UserTagsConfigSection userTags() {
        return this.userTagSection;
    }
}
