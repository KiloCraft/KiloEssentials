package org.kilocraft.essentials.extensions.customcommands.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.kilocraft.essentials.config.main.Config;
import org.kilocraft.essentials.extensions.customcommands.config.sections.CustomCommandConfigSection;

import java.util.HashMap;
import java.util.Map;

@ConfigSerializable
public class CustomCommandsConfig {
    public static String HEADER = Config.HEADER + "\n\nCustom commands";

    @Setting("commands")
    public Map<String, CustomCommandConfigSection> commands = new HashMap<String, CustomCommandConfigSection>(){{
        put("default:example", new CustomCommandConfigSection());
    }};

}
