package org.kilocraft.essentials.config.messages;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.kilocraft.essentials.config.messages.sections.GeneralConfigSection;
import org.kilocraft.essentials.config.messages.sections.commands.CommandsConfigSection;

@ConfigSerializable
public class Messages {
    @Setting(value = "general", comment = "You can use these prefixes where ever you want! just add the key behind a string\n" +
            "e.g: you can add \"${general.prefix}\" behind a string to have the value of the main prefix, like: '${general.prefix} \"Hello i'm tiny potato!\"'")
    private final GeneralConfigSection generalSection = new GeneralConfigSection();

    @Setting(value = "commands")
    private final CommandsConfigSection commandsSection = new CommandsConfigSection();

    @Setting(value = "censorList")
    private final MessageCensorList censorList = new MessageCensorList();

    public GeneralConfigSection general() {
        return this.generalSection;
    }

    public CommandsConfigSection commands() {
        return this.commandsSection;
    }

    public MessageCensorList censorList() {
        return this.censorList;
    }
}
