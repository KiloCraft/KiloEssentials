package org.kilocraft.essentials.config.main.sections.chat;

import net.minecraft.sound.SoundEvents;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class PrivateChatConfigSection {

    @Setting(value = "format")
    public String privateChat = "&7[&3%SOURCE%&r&7 -> &7%TARGET%&r&7]&f %MESSAGE%";

    @Setting(value = "meFormat")
    public String privateChatMeFormat = "&cme";

    @Setting(value = "sound", comment = "The sound you hear when someone messages you")
    private ChatPingSoundConfigSection chatPingSound = new ChatPingSoundConfigSection(SoundEvents.ENTITY_SILVERFISH_DEATH, 0.5, 1.05D);

    public ChatPingSoundConfigSection pingSound() {
        return chatPingSound;
    }
}
