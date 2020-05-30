package org.kilocraft.essentials.user.setting;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.text.MessageReceptionist;
import org.kilocraft.essentials.api.user.settting.Setting;
import org.kilocraft.essentials.api.util.StringUtils;
import org.kilocraft.essentials.chat.ServerChat;
import org.kilocraft.essentials.chat.UserMessageReceptionist;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.extensions.betterchairs.SeatManager;
import org.kilocraft.essentials.user.ServerUser;
import org.kilocraft.essentials.user.ServerUserManager;
import org.kilocraft.essentials.util.nbt.NBTTypes;
import org.kilocraft.essentials.util.nbt.NBTUtils;

import java.util.*;

public class Settings {
    @NotNull
    public static final List<Setting<?>> list = new ArrayList<>();

    public static final Setting<Boolean> CAN_FLY = new Setting<>("can_fly", false);
    public static final Setting<Boolean> INVULNERABLE = new Setting<>("invulnerable", false);
    public static final Setting<Boolean> SOCIAL_SPY = new Setting<>("social_spy", false);
    public static final Setting<Boolean> COMMAND_SPY = new Setting<>("command_spy", false);
    public static final Setting<Boolean> CAN_SEAT = new Setting<>("can_seat", false);
    public static final Setting<SeatManager.SummonType> SITTING_TYPE = new Setting<SeatManager.SummonType>(
            "sitting_type", SeatManager.SummonType.NONE,
            (fun) -> {
                if (fun.value() != null && fun.value() != SeatManager.SummonType.NONE) {
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
            "ignored", new HashMap<>(),
            (fun) -> {
                if (!fun.value().isEmpty()) {
                    ListTag list = new ListTag();
                    for (Map.Entry<String, UUID> entry : fun.value().entrySet()) {
                        CompoundTag ignored = new CompoundTag();
                        ignored.putString("name", entry.getKey());
                        NBTUtils.putUUID(ignored, "uuid", entry.getValue());

                        list.add(ignored);
                    }

                    fun.tag().put(fun.setting().getId(), list);
                }
            }, (fun) -> {
                if (fun.tag().contains(fun.setting().getId())) {
                    ListTag list = fun.tag().getList(fun.setting().getId(), NBTTypes.COMPOUND);
                    Map<String, UUID> map = new HashMap<>();
                    for (int i = 0; i < list.size(); i++) {
                        CompoundTag tag = list.getCompound(i);
                        map.put(tag.getString("name"), NBTUtils.getUUID(tag, "uuid"));
                    }
                    fun.set(map);
                }
            }
    );
    public static final Setting<Optional<String>> NICK = new Setting<Optional<String>>(
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
    public static final Setting<Integer> RANDOM_TELEPORTS_LEFT = new Setting<>("rtps_left", KiloConfig.main().rtpSpecs().defaultRTPs);
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
                    List<String> strings = new ArrayList<>();
                    ListTag listTag = fun.tag().getList(fun.setting().getId(), NBTTypes.STRING);
                    for (int i = 0; i < 10; i++) {
                        if (listTag.getCompound(i) != null && listTag.getCompound(i).contains("name")) {
                            strings.add(listTag.getCompound(i).getString("name"));
                        }
                    }

                    fun.set(strings);
                }
            }
    );
    public static final Setting<Boolean> SOUNDS = new Setting<>("sounds", true);
    public static final Setting<List<String>> PENDING_COMMANDS = new Setting<List<String>>(
            "pending_commands", Collections.emptyList(),
            (fun) -> {
                if (!fun.value().isEmpty()) {
                    ListTag listTag = new ListTag();
                    for (String s : fun.value()) {
                        CompoundTag tag = new CompoundTag();
                        tag.putString("cmd", s);
                        listTag.add(tag);
                        fun.tag().put(fun.setting().getId(), listTag);
                    }
                }
            }, (fun) -> {
                if (fun.tag().contains(fun.setting().getId())) {
                    List<String> strings = new ArrayList<>();
                    ListTag listTag = fun.tag().getList(fun.setting().getId(), NBTTypes.STRING);
                    for (int i = 0; i < 10; i++) {
                        if (listTag.getCompound(i) != null && listTag.getCompound(i).contains("cmd")) {
                            strings.add(listTag.getCompound(i).getString("cmd"));
                        }
                    }

                    fun.set(strings);
                }
    });

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
