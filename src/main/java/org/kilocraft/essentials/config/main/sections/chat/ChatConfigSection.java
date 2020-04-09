package org.kilocraft.essentials.config.main.sections.chat;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class ChatConfigSection {
    @Setting(value = "commandSpyFormat", comment = "Set the format of command spy messages")
    public String commandSpyFormat = "&r&7%SOURCE% &3->&r /%COMMAND%";

    @Setting(value = "useVanillaChat", comment = "If set to true the KiloEssentials Chat will be disabled")
    public boolean useVanillaChat = false;

    @Setting(value = "kickForUsingIllegalCharacters", comment = "Kicks a player if they use Illegal Characters in the chat\n If set to false they will only get a warning")
    public boolean kickForUsingIllegalCharacters = false;

    @Setting(value = "kickForSpamming", comment = "Kicks a player if they try to spam the chat\nIf set to false they will only get a warning")
    public boolean kickForSpamming = false;

    @Setting(value = "itemFormat", comment = "The format someone has to use to show an Item (Main hand) in chat")
    public String itemFormat = "[item]";

    @Setting(value = "prefixes", comment = "Sets format and meta of chat channels")
    private ChatPrefixesConigSection prefixesSection = new ChatPrefixesConigSection();

    @Setting(value = "privateChat", comment = "Sets the format and meta of private chat channels")
    private PrivateChatConfigSection privateChatSection = new PrivateChatConfigSection();

    @Setting(value = "ping")
    private ChatPingConfigSection pingSection = new ChatPingConfigSection();

    public ChatPrefixesConigSection prefixes() {
        return prefixesSection;
    }

    public PrivateChatConfigSection privateChat() {
        return privateChatSection;
    }

    public ChatPingConfigSection ping() {
        return pingSection;
    }

}
