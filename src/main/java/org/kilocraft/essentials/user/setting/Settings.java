package org.kilocraft.essentials.user.setting;

import com.sun.org.apache.xpath.internal.operations.Bool;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.GameMode;
import org.kilocraft.essentials.api.user.settting.Setting;
import org.kilocraft.essentials.chat.channels.GlobalChat;
import org.kilocraft.essentials.util.NBTUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Settings {
    public static final Setting<Boolean> CAN_FLY = new Setting<>("can_fly", false);
    public static final Setting<Boolean> INVULNERABLE = new Setting<>("invulnerable", false);
    public static final Setting<Boolean> SOCIAL_SPY = new Setting<>("social_spy", false);
    public static final Setting<Boolean> COMMAND_SPY = new Setting<>("command_spy", false);
    public static final Setting<Boolean> CAN_SEAT = new Setting<>("can_sit", false);
    public static final Setting<GameMode> GAME_MODE = new Setting<GameMode>(
            "gamemode", GameMode.NOT_SET,
            (fun) -> {
                fun.tag().putString(fun.setting().getId(), fun.value().getName());
            }, (fun) -> {
                fun.set(GameMode.valueOf(fun.setting().getId()));
    });
    public static final Setting<Map<String, UUID>> IGNORE_LIST = new Setting<Map<String, UUID>>(
            "ignore_list", new HashMap<>(),
            (fun) -> {
                ListTag listTag = new ListTag();
                fun.value().forEach((name, uuid) -> {
                    CompoundTag ignoredOne = new CompoundTag();
                    NBTUtils.putUUID(ignoredOne, "uuid", uuid);
                    ignoredOne.putString("name", name);
                    listTag.add(ignoredOne);
                });

                fun.tag().put(fun.setting().getId(), listTag);
            }, (fun) -> {
                ListTag listTag = fun.tag().getList(fun.setting().getId(), 10);
                for (int i = 0; i < listTag.size(); i++) {
                    Map<String, UUID> map = new HashMap<>();
                    CompoundTag ignoredOne = listTag.getCompound(i);
                    map.put(ignoredOne.getString("name"), NBTUtils.getUUID(ignoredOne, "uuid"));
                    fun.set(map);
                }
    });
    public static final Setting<String> UP_STREAM_CHANNEL = new Setting<>("up_stream_channel", GlobalChat.getChannelId());
    public static final Setting<Integer> RANDOM_TELEPORTS_LEFT = new Setting<>("rtps_left", 0);
    public static final Setting<Boolean> DON_NOT_DISTURB = new Setting<>("do_not_disturb", false);
}
