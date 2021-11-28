package org.kilocraft.essentials.config.main.sections.chat;

import net.minecraft.sounds.SoundEvents;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public class PrivateChatConfigSection {

    @Setting(value = "format")
    public String privateChat = "&7[&3%SOURCE%&r&7 -> &7%TARGET%&r&7]&f %MESSAGE%";

    @Setting(value = "meFormat")
    public String privateChatMeFormat = "&cme";

    @Setting(value = "sound")
    @Comment("The sound you hear when someone messages you")
    private final ChatPingSoundConfigSection chatPingSound = new ChatPingSoundConfigSection(SoundEvents.SILVERFISH_DEATH, 0.09, 1.05D);

    public ChatPingSoundConfigSection pingSound() {
        return this.chatPingSound;
    }
}
