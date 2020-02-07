package org.kilocraft.essentials.config.messages.sections.commands;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class CommandsContextConfigSection {

    @Setting(value = "executionException", comment = "The message you get if a command fails because of a syntax error")
    public String executionException = "&cUnknown or incomplete command! Type '/help' for help";

    @Setting(value = "permissionException", comment = "The messages you get if a command fails because of not having the requirements")
    public String permissionException = "&cInsufficient permission! You can't use that command";

}
