package org.kilocraft.essentials.user.preference;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.user.preference.Preference;
import org.kilocraft.essentials.chat.ServerChat;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.extensions.betterchairs.SeatManager;
import org.kilocraft.essentials.util.nbt.NBTTypes;

import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

public class Preferences {
    @NotNull
    public static final List<Preference<?>> list = new ArrayList<>();

    public static final Preference<Boolean> INVULNERABLE = new Preference<>("invulnerable", false);
    public static final Preference<Boolean> SOCIAL_SPY = new Preference<>("social_spy", false);
    public static final Preference<Boolean> COMMAND_SPY = new Preference<>("command_spy", false);
    public static final Preference<Boolean> CAN_SEAT = new Preference<>("can_seat", false);
    public static final Preference<SeatManager.SummonType> SITTING_TYPE = new Preference<SeatManager.SummonType>(
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
    public static final Preference<Optional<String>> NICK = new Preference<Optional<String>>(
            "nickname", Optional.empty(),
            (fun) -> fun.value().ifPresent((nickname) -> fun.tag().putString(fun.setting().getId(), nickname)),
            (fun) -> {
                if (fun.tag().contains(fun.setting().getId())) {
                    fun.set(Optional.of(fun.tag().getString(fun.setting().getId())));
                }
            }
    );
    public static final Preference<ServerChat.Channel> CHAT_CHANNEL = new Preference<ServerChat.Channel>(
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
    public static final Preference<Integer> RANDOM_TELEPORTS_LEFT = new Preference<>("rtps_left", KiloConfig.main().rtpSpecs().defaultRTPs);
    public static final Preference<Boolean> DON_NOT_DISTURB = new Preference<>("do_not_disturb", false);
    public static final Preference<Boolean> VANISH = new Preference<>("vanish", false);
    public static final Preference<List<String>> FAVIORATE_PLAYER_WARPS = new Preference<List<String>>(
            "fav_pwarps", Collections.emptyList(),
            (fun) -> {
                if (!fun.value().isEmpty()) {
                    ListTag NbtList = new ListTag();
                    for (String s : fun.value()) {
                        CompoundTag tag = new CompoundTag();
                        tag.putString("name", s);
                        NbtList.add(tag);
                        fun.tag().put(fun.setting().getId(), NbtList);
                    }
                }
            }, (fun) -> {
        if (fun.tag().contains(fun.setting().getId())) {
            List<String> strings = new ArrayList<>();
            ListTag NbtList = fun.tag().getList(fun.setting().getId(), NBTTypes.STRING);
            for (int i = 0; i < 10; i++) {
                if (NbtList.getCompound(i) != null && NbtList.getCompound(i).contains("name")) {
                    strings.add(NbtList.getCompound(i).getString("name"));
                }
            }

            fun.set(strings);
        }
    }
    );
    public static final Preference<Boolean> SOUNDS = new Preference<>("sounds", true);
    public static final Preference<List<String>> PENDING_COMMANDS = new Preference<List<String>>(
            "pending_commands", Collections.emptyList(),
            (fun) -> {
                if (!fun.value().isEmpty()) {
                    ListTag NbtList = new ListTag();
                    for (String s : fun.value()) {
                        CompoundTag tag = new CompoundTag();
                        tag.putString("cmd", s);
                        NbtList.add(tag);
                        fun.tag().put(fun.setting().getId(), NbtList);
                    }
                }
            }, (fun) -> {
        if (fun.tag().contains(fun.setting().getId())) {
            List<String> strings = new ArrayList<>();
            ListTag NbtList = fun.tag().getList(fun.setting().getId(), NBTTypes.STRING);
            for (int i = 0; i < 10; i++) {
                if (NbtList.getCompound(i) != null && NbtList.getCompound(i).contains("cmd")) {
                    strings.add(NbtList.getCompound(i).getString("cmd"));
                }
            }

            fun.set(strings);
        }
    });
    public static final Preference<List<ServerChat.Channel>> DISABLED_CHATS = new Preference<List<ServerChat.Channel>>(
            "enabled_chats", Lists.newArrayList(),
            (fun) -> {
                if (!fun.value().equals(fun.setting().getDefault())) {
                    ListTag list = new ListTag();
                    for (ServerChat.Channel channel : fun.value()) {
                        CompoundTag tag = new CompoundTag();
                        tag.putString("id", channel.getId());
                        list.add(tag);
                    }
                    fun.tag().put(fun.setting().getId(), list);
                }
            }, (fun) -> {
        if (fun.tag().contains(fun.setting().getId())) {
            List<ServerChat.Channel> channels = Lists.newArrayList();
            ListTag list = fun.tag().getList(fun.setting().getId(), NBTTypes.COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag tag = list.getCompound(i);
                ServerChat.Channel channel = ServerChat.Channel.getById(tag.getString("id"));
                if (channel != null) {
                    channels.add(channel);
                }
            }
            fun.set(channels);
        }
    }
    );
    public static final Preference<ServerChat.VisibilityPreference> CHAT_VISIBILITY = new Preference<ServerChat.VisibilityPreference>(
            "chat_visibility", ServerChat.VisibilityPreference.ALL,
            (fun) -> {
                if (!fun.value().equals(fun.setting().getDefault())) {
                    fun.tag().putString("visibility", fun.value().toString());
                }
            }, (fun) -> {
        if (fun.tag().contains("visibility")) {
            ServerChat.VisibilityPreference preference = ServerChat.VisibilityPreference.getByName(fun.tag().getString("visibility"));
            if (preference != null) {
                fun.set(preference);
            }
        }
    }
    );

    @Nullable
    public static Preference<?> getById(String id) {
        for (Preference<?> preference : list) {
            if (preference.getId().equalsIgnoreCase(id)) {
                return preference;
            }
        }

        return null;
    }
}
