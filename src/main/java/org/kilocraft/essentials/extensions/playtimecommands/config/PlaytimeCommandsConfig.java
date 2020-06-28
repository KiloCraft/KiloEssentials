package org.kilocraft.essentials.extensions.playtimecommands.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.kilocraft.essentials.config.main.Config;
import org.kilocraft.essentials.extensions.customcommands.config.CustomCommandsConfig;

import java.util.ArrayList;
import java.util.List;

@ConfigSerializable
public class PlaytimeCommandsConfig {

    public static final String HEADER = Config.HEADER + "Schedule different commands to run when someone reaches a specific Playtime!\n" +
            "the \"sudo <player> util\" command has a few special utilities! be sure to check it out!\n" + CustomCommandsConfig.COMMANDS_DESC;

    @Setting(value = "sections", comment = "Variables: ${user.name}, ${user.displayname}, ${user.ranked_displayname}")
    public List<PlaytimeCommandConfigSection> sections = new ArrayList<PlaytimeCommandConfigSection>(){{
        this.add(new PlaytimeCommandConfigSection());
    }};

}
