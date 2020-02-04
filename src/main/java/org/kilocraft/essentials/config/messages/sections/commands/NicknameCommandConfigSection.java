package org.kilocraft.essentials.config.messages.sections.commands;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class NicknameCommandConfigSection {

    @Setting(value = "setSelf")
    public String setSelf = "&eSet the nickname to &6%NICK_NEW%&e from &6%NICK%";

    @Setting(value = "resetSelf")
    public String resetSelf = "&eYou have &6reset&e your nickname";

    @Setting(value = "setOthers")
    public String setOthers = "&eSet &6%TARGET_TAG%&e's nickname to &6%NICK_NEW%&e from &6%NICK%&e";

    @Setting(value = "resetOthers")
    public String resetOthers = "&eYou have reset &6%TARGET_TAG%&e's nickname";

}
