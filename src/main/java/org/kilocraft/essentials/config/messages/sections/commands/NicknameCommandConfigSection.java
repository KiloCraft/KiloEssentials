package org.kilocraft.essentials.config.messages.sections.commands;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class NicknameCommandConfigSection {

    @Setting(value = "setSelf")
    public String setSelf = "<yellow>Set the nickname to <gold>{NICK_NEW}<reset> <yellow>from <gold>{NICK}";

    @Setting(value = "resetSelf")
    public String resetSelf = "<yellow>You have <gold>reset<yellow> your nickname";

    @Setting(value = "setOthers")
    public String setOthers = "<yellow>Set the nickname to <gold>{NICK_NEW}<yellow> from <gold>{NICK}<yellow> for <gold>{TARGET_TAG}";

    @Setting(value = "resetOthers")
    public String resetOthers = "<yellow>You have reset the nickname for <gold>{TARGET_TAG}";

}
