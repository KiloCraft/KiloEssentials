package org.kilocraft.essentials.extensions.rankmanager.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class RankConfigSection {

    @Setting(value = "meta")
    private RankMetaConfigSection metaSection = new RankMetaConfigSection();

    @Setting(value = "permissions")
    private RankPermissionsConfigSection permissionsSection = new RankPermissionsConfigSection();

    @Setting
    public String displayName = "Default";

    public RankMetaConfigSection getMeta() {
        return metaSection;
    }

    public RankPermissionsConfigSection getPermissions() {
        return permissionsSection;
    }

}
