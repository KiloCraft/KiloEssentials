package org.kilocraft.essentials.user.setting;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.user.settting.Setting;
import org.kilocraft.essentials.chat.ServerChat;
import org.kilocraft.essentials.extensions.betterchairs.SeatManager;
import org.kilocraft.essentials.util.NBTUtils;

import java.util.*;

public class Settings {
    @NotNull
    public static final List<Setting<?>> list = new ArrayList<>();

    public static final Setting<Boolean> CAN_FLY = new Setting<>("can_fly", false);
    public static final Setting<Boolean> INVULNERABLE = new Setting<>("invulnerable", false);
    public static final Setting<Boolean> SOCIAL_SPY = new Setting<>("social_spy", false);
    public static final Setting<Boolean> COMMAND_SPY = new Setting<>("command_spy", false);
    public static final Setting<Boolean> CAN_SEAT = new Setting<>("can_seat", false);
    public static final Setting<SeatManager.SummonType> SEATING_TYPE = new Setting<SeatManager.SummonType>(
            "seating_type", SeatManager.SummonType.NONE,
            (fun) -> {
                if (fun.value() != SeatManager.SummonType.NONE) {
                    fun.tag().putString(fun.setting().getId(), fun.value().toString().toLowerCase(Locale.ROOT));
                }
            },
            (fun) -> {
                if (fun.tag().contains(fun.setting().getId())) {
                    fun.set(SeatManager.SummonType.getByName(fun.tag().getString(fun.setting().getId())));
                }
            });
    public static final Setting<GameMode> GAME_MODE = new Setting<GameMode>(
            "gamemode", GameMode.NOT_SET,
            (fun) -> fun.tag().putInt(fun.setting().getId(), fun.value().getId()),
            (fun) -> fun.set(GameMode.byId(fun.tag().getInt(fun.setting().getId())))
    );
    public static final Setting<Map<String, UUID>> IGNORE_LIST = new Setting<Map<String, UUID>>(
            "ignore_list", new HashMap<>(),
            (fun) -> {
                if (fun.value().isEmpty()) {
                    return;
                }

                ListTag listTag = new ListTag();
                fun.value().forEach((name, uuid) -> {
                    CompoundTag ignoredOne = new CompoundTag();
                    NBTUtils.putUUID(ignoredOne, "uuid", uuid);
                    ignoredOne.putString("name", name);
                    listTag.add(ignoredOne);
                });

                fun.tag().put(fun.setting().getId(), listTag);
            }, (fun) -> {
                if (!fun.tag().contains(fun.setting().getId())) {
                    return;
                }

                ListTag listTag = fun.tag().getList(fun.setting().getId(), 10);
                for (int i = 0; i < listTag.size(); i++) {
                    Map<String, UUID> map = new HashMap<>();
                    CompoundTag ignoredOne = listTag.getCompound(i);
                    map.put(ignoredOne.getString("name"), NBTUtils.getUUID(ignoredOne, "uuid"));
                    fun.set(map);
                }
            }
    );
    public static final Setting<Optional<String>> NICKNAME = new Setting<Optional<String>>(
            "nickname", Optional.empty(),
            (fun) -> fun.value().ifPresent((nickname) -> fun.tag().putString(fun.setting().getId(), nickname)),
            (fun) -> {
                if (fun.tag().contains(fun.setting().getId())) {
                    fun.set(Optional.of(fun.tag().getString(fun.setting().getId())));
                }
            }
    );
    public static final Setting<ServerChat.Channel> CHAT_CHANNEL = new Setting<ServerChat.Channel>(
            "chat_channel", ServerChat.Channel.PUBLIC,
            (fun) -> {
                if (!fun.value().equals(ServerChat.Channel.PUBLIC)) {
                    fun.tag().putString(fun.tag().getString(fun.setting().getId()), fun.value().getId());
                }
            }, (fun) -> {
                if (!fun.tag().contains(fun.setting().getId())) {
                    fun.set(ServerChat.Channel.PUBLIC);
                } else {
                    fun.set(ServerChat.Channel.getById(fun.tag().getString(fun.setting().getId())));
                }
            }
    );
    public static final Setting<Integer> RANDOM_TELEPORTS_LEFT = new Setting<>("rtps_left", 3);
    public static final Setting<Boolean> DON_NOT_DISTURB = new Setting<>("do_not_disturb", false);
    public static final Setting<List<String>> FAVIORATE_PLAYER_WARPS = new Setting<List<String>>(
            "fav_pwarps", Collections.emptyList(),
            (fun) -> {
                if (!fun.value().isEmpty()) {
                    ListTag listTag = new ListTag();
                    for (String s : fun.value()) {
                        CompoundTag tag = new CompoundTag();
                        tag.putString("name", s);
                        listTag.add(tag);
                        fun.tag().put(fun.setting().getId(), listTag);
                    }
                }
            }, (fun) -> {
                if (fun.tag().contains(fun.setting().getId())) {
                    ListTag listTag = fun.tag().getList(fun.setting().getId(), 8);
                    //TODO: Fish the De-Serializer
                }
            }
    );
    public static final Setting<Boolean> SOUNDS = new Setting<>("sounds", true);

    @Nullable
    public static Setting<?> getById(String id) {
        for (Setting<?> setting : list) {
            if (setting.getId().equalsIgnoreCase(id)) {
                return setting;
            }
        }

        return null;
    }
}
