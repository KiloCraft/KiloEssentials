package org.kilocraft.essentials.config.main.sections.chat;


import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public class ChatConfigSection {

    @Setting(value = "shouldCensor")
    @Comment("If set to true all words in censored.txt will get censored")
    public boolean shouldCensor = true;

    @Setting(value = "useVanillaChat")
    @Comment("If set to true the KiloEssentials Chat will be disabled")
    public boolean useVanillaChat = false;

    @Setting(value = "prefixes")
    @Comment("Sets format and meta of chat channels")
    private final ChatFormatsConfigSection formatsSection = new ChatFormatsConfigSection();

    @Setting(value = "privateChat")
    @Comment("Sets the format and meta of private chat channels")
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
