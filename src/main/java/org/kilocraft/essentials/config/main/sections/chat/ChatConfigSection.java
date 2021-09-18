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

    @Setting(value = "prefixes", comment = "Sets format and meta of chat channels")
    private final ChatFormatsConfigSection formatsSection = new ChatFormatsConfigSection();

    @Setting(value = "privateChat", comment = "Sets the format and meta of private chat channels")
    private final PrivateChatConfigSection privateChatSection = new PrivateChatConfigSection();

    @Setting(value = "ping")
    private final ChatPingConfigSection pingSection = new ChatPingConfigSection();

    public ChatFormatsConfigSection prefixes() {
        return this.formatsSection;
    }

    public PrivateChatConfigSection privateChat() {
        return this.privateChatSection;
    }

    public ChatPingConfigSection ping() {
        return this.pingSection;
    }

}
