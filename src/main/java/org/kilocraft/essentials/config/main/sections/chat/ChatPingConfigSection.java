package org.kilocraft.essentials.config.main.sections.chat;

import net.minecraft.sounds.SoundEvents;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public class ChatPingConfigSection {

    @Setting(value = "sound")
    @Comment("The sound you hear when someone pings you")
    private final ChatPingSoundConfigSection chatPingSound = new ChatPingSoundConfigSection(SoundEvents.EXPERIENCE_ORB_PICKUP, 3.0D, 1.0D);

    public ChatPingSoundConfigSection pingSound() {
        return this.chatPingSound;
    }
}
