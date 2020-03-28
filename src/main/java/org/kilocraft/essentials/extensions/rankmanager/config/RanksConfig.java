package org.kilocraft.essentials.extensions.rankmanager.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.kilocraft.essentials.config.main.Config;

import java.util.HashMap;
import java.util.Map;

@ConfigSerializable
public class RanksConfig {
    public static final String HEADER = Config.HEADER + "";

    @Setting(value = "ranks")
    public Map<String, RankConfigSection> ranks = new HashMap<String, RankConfigSection>(){{
        put("default", new RankConfigSection());
    }};
}
