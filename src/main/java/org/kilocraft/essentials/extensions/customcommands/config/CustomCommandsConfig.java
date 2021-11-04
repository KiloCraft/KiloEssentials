package org.kilocraft.essentials.extensions.customcommands.config;

import org.kilocraft.essentials.config.main.Config;
import org.kilocraft.essentials.extensions.customcommands.config.sections.CustomCommandConfigSection;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.ArrayList;
import java.util.List;

@ConfigSerializable
public class CustomCommandsConfig {
    public static final String COMMANDS_DESC = "Put a \"!\" at the start of the command to run with Operator permissions\n" +
            "put a \"?\" at the start to run as the Server";
    public static final String HEADER = Config.HEADER + "\n\nCustom commands\n" + COMMANDS_DESC;

    @Setting("commands")
    public List<CustomCommandConfigSection> commands = new ArrayList<>() {{
        this.add(new CustomCommandConfigSection());
    }};

}
