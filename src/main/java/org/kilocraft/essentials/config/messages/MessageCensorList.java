package org.kilocraft.essentials.config.messages;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.ArrayList;
import java.util.List;

@ConfigSerializable
public class MessageCensorList {
    @Setting(value = "words", comment = "A list of words to censor")
    public List<String> words = new ArrayList<String>() {{
        this.add("KiloEssentialsIsBad");
    }};

    @Setting(value = "alternateChar", comment = "The character to replace the words with, Default: \"*\"")
    public String alternateChar = "*";

    @Setting(value = "censorDirectMessages", comment = "Enables message censoring in Direct (Private) messages")
    public boolean censorDirectMessages = true;

    @Setting(value = "censorPrivateChannels", comment = "Enables message censoring in private channels like staff and builder")
    public boolean censorPrivateChannels = false;

    @Setting(value = "censor", comment = "Censor the words instead of blocking the message")
    public boolean censor = true;

    @Setting(value = "blockMessage", comment = "Set the block message if the message contains one of the words and censor is set to false, Values: \"%s\"")
    public String blockMessage = "&cOi %s&c! Mind your language!";
}
