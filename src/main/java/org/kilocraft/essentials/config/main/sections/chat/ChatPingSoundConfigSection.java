package org.kilocraft.essentials.config.main.sections.chat;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.Objects;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

@ConfigSerializable
public class ChatPingSoundConfigSection {

    @Setting(value = "enabled")
    @Comment("Enable or disable the Chat Ping Sound")
    public boolean enabled = true;

    @Setting(value = "id")
    @Comment("Sound identifier, you can search for them through the '/playsound' command in the game!")
    public String id;

    @Setting(value = "volume")
    @Comment("The volume of the sound, Can be between 0 and 3")
    public double volume;

    @Setting(value = "pitch")
    @Comment("Pitch of the sound, can be between 0 and 3, Default: 1.0")
    public double pitch;

    public ChatPingSoundConfigSection() {
        this.id = Objects.requireNonNull(Registry.SOUND_EVENT.getKey(SoundEvents.EXPERIENCE_ORB_PICKUP)).toString();
        this.volume = 3.0;
        this.pitch = 1.0;
    }

    public ChatPingSoundConfigSection(SoundEvent event, double volume, double pitch) {
        ResourceLocation identifier = Registry.SOUND_EVENT.getKey(event);
        this.id = identifier != null ? identifier.toString() : "entity.experience_orb.pickup";
        this.volume = volume;
        this.pitch = pitch;
    }
}
