package org.kilocraft.essentials.config.main.sections.chat;

import net.minecraft.sound.SoundEvents;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class ChatPingConfigSection {

    @Setting(value = "enabled", comment = "Pong! by enabling this players can ping each other inside the game!\nthey need to have the permission node \"kiloessentials.chat.ping\"")
    public boolean enabled = true;

    @Setting(value = "everyoneTypedFormat", comment = "This is how you have to type the format in the chat to ping everyone!")
    public String everyoneTypedFormat = "@everyone";

    @Setting(value = "everyonePingedFormat", comment = "This is how you see the everone typed format in the chat")
    public String everyonePingedFormat = "&b&o@everyone";

    @Setting(value = "typedFormat", comment = "This is how you have to type someone's name in order to ping them")
    public String typedFormat = "%PLAYER_NAME%";

    @Setting(value = "pingedFormat", comment = "This is how you see the typed format in the chat")
    public String pingedFormat = "&a&o%PLAYER_DISPLAYNAME%";

    @Setting(value = "pingedNotPingedFormat", comment = "The format of the not pinged player's name! This will be used if the pinging that player is not allowed or if it failed")
    public String pingedNotPingedFormat = "&a&o&m%PLAYER_DISPLAYNAME%";

    @Setting(value = "sound", comment = "The sound you hear when someone pings you")
    private ChatPingSoundConfigSection chatPingSound = new ChatPingSoundConfigSection(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 3.0D, 1.0D);

    public ChatPingSoundConfigSection pingSound() {
        return chatPingSound;
    }
}
