package org.kilocraft.essentials.extensions.playtimecommands.config;

import org.kilocraft.essentials.config.main.Config;
import org.kilocraft.essentials.extensions.customcommands.config.CustomCommandsConfig;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.ArrayList;
import java.util.List;

@ConfigSerializable
public class PlaytimeCommandsConfig {

    public static final String HEADER = Config.HEADER + "Schedule different commands to run when someone reaches a specific Playtime!\n" +
            "the \"sudo <player> util\" command has a few special utilities! be sure to check it out!\n" + CustomCommandsConfig.COMMANDS_DESC;

    @Setting(value = "sections")
    @Comment("Variables: ${user.name}, ${user.displayname}, ${user.ranked_displayname}")
    public List<PlaytimeCommandConfigSection> sections = new ArrayList<PlaytimeCommandConfigSection>() {{
        this.add(new PlaytimeCommandConfigSection());
    }};

}
