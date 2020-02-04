package org.kilocraft.essentials.config.sections.chat;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class ChatConfigSection {

    @Setting(value = "useVanillaChat", comment = "If set to true the KiloEssentials Chat will be disabled")
    public boolean useVanillaChat = false;

    @Setting(value = "kickForUsingIllegalCharacters", comment = "Kicks a player if they use Illegal Characters in the chat\n If set to false they will only get a warning")
    public boolean kickForUsingIllegalCharacters = false;

    @Setting(value = "channelsMeta", comment = "Sets format and meta of chat channels")
    private ChannelMetaConfigSection channelMetaSection = new ChannelMetaConfigSection();

    @Setting(value = "privateChat", comment = "Sts the format and meta of private chat channels")
    private PrivateChatConfigSection privateChatSection = new PrivateChatConfigSection();

    @Setting(value = "ping")
    private ChatPingConfigSection pingSection = new ChatPingConfigSection();

    public ChannelMetaConfigSection channelMeta() {
        return channelMetaSection;
    }

    public PrivateChatConfigSection privateChat() {
        return privateChatSection;
    }

    public ChatPingConfigSection ping() {
        return pingSection;
    }

}
