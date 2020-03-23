package org.kilocraft.essentials.user.setting;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.GameMode;
import org.kilocraft.essentials.api.user.settting.Setting;
import org.kilocraft.essentials.chat.channels.GlobalChat;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Settings {
    public static final Setting<Boolean> CAN_FLY = new Setting<>("can_fly", false);
    public static final Setting<Boolean> INVULNERABLE = new Setting<>("invulnerable", false);
    public static final Setting<Boolean> SOCIAL_SPY = new Setting<>("social_spy", false);
    public static final Setting<Boolean> COMMAND_SPY = new Setting<>("command_spy", false);
    public static final Setting<Boolean> CAN_SEAT = new Setting<>("can_sit", false);
    public static final Setting<GameMode> GAME_MODE = new Setting<>("gamemode", GameMode.NOT_SET);
    public static final Setting<Map<String, UUID>> IGNORE_LIST = new Setting<>("ignore_list", new HashMap<>());
    public static final Setting<String> UP_STREAM_CHANNEL = new Setting<>("up_stream_channel", GlobalChat.getChannelId());

    public static <T> CompoundTag toTag(Setting<T> setting, T value) {
        CompoundTag tag = new CompoundTag();

        if (value instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) value;
            map.forEach((k, v) -> {

            });
        }

        return tag;
    }

}
