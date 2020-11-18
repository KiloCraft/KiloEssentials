package org.kilocraft.essentials.config.main.sections.chat;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class ChatConfigSection {
    @Setting(value = "commandSpyFormat", comment = "Set the format of command spy messages")
    public String commandSpyFormat = "&8[&5Spy&8] &r&7%SOURCE% &3->&r /%COMMAND%";

    @Setting(value = "socialSpyFormat", comment = "Set the format of social spy messages")
    public String socialSpyFormat = "&8[&5Spy&8] &7[%SOURCE%&r&3 -> &7%TARGET%&r&7]&7 %MESSAGE%";

    @Setting(value = "useVanillaChat", comment = "If set to true the KiloEssentials Chat will be disabled")
    public boolean useVanillaChat = false;

    @Setting(value = "kickForUsingIllegalCharacters", comment = "Kicks a player if they use Illegal Characters in the chat\n If set to false they will only get a warning")
    public boolean kickForUsingIllegalCharacters = false;

    @Setting(value = "kickForSpamming", comment = "Kicks a player if they try to spam the chat\nIf set to false they will only get a warning")
    public boolean kickForSpamming = true;

    @Setting(value = "spamWarning", comment = "Send this message to the player if they are trying to spam")
    public String spamWarning = "&cDon't spam!";

    @Setting(value = "itemFormat", comment = "The format someone has to use to show an Item (Main hand) in chat (regex)")
    public String itemFormat = "\\[item\\]";

    @Setting(value = "prefixes", comment = "Sets format and meta of chat channels")
    private ChatFormatsConfigSection formatsSection = new ChatFormatsConfigSection();

    @Setting(value = "privateChat", comment = "Sets the format and meta of private chat channels")
    private PrivateChatConfigSection privateChatSection = new PrivateChatConfigSection();

    @Setting(value = "ping")
    private ChatPingConfigSection pingSection = new ChatPingConfigSection();

    public ChatFormatsConfigSection prefixes() {
        return formatsSection;
    }

    public PrivateChatConfigSection privateChat() {
        return privateChatSection;
    }

    public ChatPingConfigSection ping() {
        return pingSection;
    }

}
