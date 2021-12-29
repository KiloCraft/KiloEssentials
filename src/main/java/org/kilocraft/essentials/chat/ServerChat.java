package org.kilocraft.essentials.chat;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.config.main.sections.chat.ChatConfigSection;
import org.kilocraft.essentials.config.main.sections.chat.ChatFormatsConfigSection;
import org.kilocraft.essentials.config.main.sections.chat.ChatPingSoundConfigSection;
import org.kilocraft.essentials.events.ChatEvents;
import org.kilocraft.essentials.user.CommandSourceServerUser;
import org.kilocraft.essentials.user.OnlineServerUser;
import org.kilocraft.essentials.user.ServerUser;
import org.kilocraft.essentials.user.ServerUserManager;
import org.kilocraft.essentials.user.preference.Preferences;
import org.kilocraft.essentials.util.EssentialPermission;
import org.kilocraft.essentials.util.commands.CommandUtils;
import org.kilocraft.essentials.util.commands.KiloCommands;
import org.kilocraft.essentials.util.text.Texter;

import java.util.*;
import java.util.function.Predicate;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundCustomSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

public final class ServerChat {
    private static final int COMMAND_MAX_LENGTH = 45;
    private static final SimpleCommandExceptionType CANT_MESSAGE_EXCEPTION = new SimpleCommandExceptionType(StringText.of("command.message.error"));
    private static final ChatConfigSection config = KiloConfig.main().chat();

    public static void sendChatMessage(final OnlineUser user, final String raw, final Channel channel) {
        ChatMessage message = new ChatMessage(user, raw, channel);
        message.send();
    }

    public static void pingUser(final OnlineUser target, final MentionTypes type) {
        if (target.getPreference(Preferences.DON_NOT_DISTURB)) {
            return;
        }

        ChatPingSoundConfigSection cfg = switch (type) {
            case PUBLIC -> config.ping().pingSound();
            case PRIVATE -> config.privateChat().pingSound();
        };

        pingUser(target.asPlayer(), cfg);
    }

    private static void pingUser(final ServerPlayer target, final ChatPingSoundConfigSection cfg) {
        Vec3 vec3d = target.createCommandSourceStack().getPosition();
        if (target.connection != null) {
            target.connection.send(
                    new ClientboundCustomSoundPacket(
                            new ResourceLocation(cfg.id),
                            SoundSource.MASTER,
                            vec3d, (float) cfg.volume, (float) cfg.pitch)
            );
        }
    }

    public static int sendDirectMessage(final CommandSourceStack source, final OnlineUser target, final String message) throws CommandSyntaxException {
        CommandSourceUser src = CommandSourceServerUser.of(source);

        if (!((ServerUser) target).shouldMessage() && src.getUser() != null) {
            if (!src.isConsole() && src.isOnline() && !((ServerUser) src.getUser()).isStaff()) {
                throw ServerChat.CANT_MESSAGE_EXCEPTION.create();
            }
        }

        if (!CommandUtils.isConsole(source)) {
            OnlineUser online = KiloEssentials.getUserManager().getOnline(source);
            online.setLastMessageReceptionist(target);
            target.setLastMessageReceptionist(online);
        }

        if (CommandUtils.areTheSame(source, target)) {
            throw KiloCommands.getException("exception.source_is_target").create();
        }

        ServerChat.messagePrivately(source, target, message);
        return 1;
    }

    public static void messagePrivately(final CommandSourceStack source, final OnlineUser target, final String raw) throws CommandSyntaxException {
        String format = ServerChat.config.privateChat().privateChat;
        String me_format = ServerChat.config.privateChat().privateChatMeFormat;
        String sourceName = source.getTextName();

        if (CommandUtils.isPlayer(source)) {
            OnlineUser user = KiloEssentials.getUserManager().getOnline(source);
            if (KiloEssentials.getUserManager().getPunishmentManager().isMuted(user.getUuid())) {
                user.sendMessage(ServerUserManager.getMuteMessage(user));
                return;
            }
        }

        String toSource = format.replace("%SOURCE%", me_format)
                .replace("%TARGET%", "&r" + target.getUsername() + "&r")
                .replace("%MESSAGE%", raw);
        String toTarget = format.replace("%SOURCE%", sourceName)
                .replace("%TARGET%", me_format)
                .replace("%MESSAGE%", raw);

        /*String toSpy = ServerChat.config.socialSpyFormat.replace("%SOURCE%", sourceName)
                .replace("%TARGET%", target.getUsername() + "&r")
                .replace("%MESSAGE%", raw);*/
        String toSpy = ModConstants.translation("chat.social_spy", sourceName, target.getUsername(), raw);

        if (target.getPreference(Preferences.SOUNDS)) {
            pingUser(target, MentionTypes.PRIVATE);
        }

        ComponentText.toText(ComponentText.removeEvents(ComponentText.of(toSource)));

        CommandSourceServerUser.of(source).sendMessage(toSource);
        target.sendMessage(toTarget);

        for (final OnlineServerUser user : KiloEssentials.getUserManager().getOnlineUsers().values()) {
            if (user.getPreference(Preferences.SOCIAL_SPY) && !CommandUtils.areTheSame(source, user) && !CommandUtils.areTheSame(target, user)) {
                user.sendMessage(toSpy);
            }

            ChatEvents.DIRECT_MESSAGE.invoker().onDirectMessage(source, target, raw);
        }

        KiloChat.broadCastToConsole(toSpy);
    }

    public static void sendCommandSpy(final CommandSourceStack source, final String command) {
        String shortenedCommand = command.substring(0, Math.min(command.length(), COMMAND_MAX_LENGTH));

        if (command.length() > COMMAND_MAX_LENGTH) {
            shortenedCommand += "...";
        }

        for (OnlineServerUser user : KiloEssentials.getUserManager().getOnlineUsers().values()) {
            if (user.getPreference(Preferences.COMMAND_SPY) && !CommandUtils.areTheSame(source, user)) {
                user.sendLangMessage("general.social_spy", source.getTextName(), shortenedCommand);
            }
        }
    }

    public enum Channel {
        PUBLIC("public"),
        STAFF("staff", EssentialPermission.STAFF),
        BUILDER("builder", EssentialPermission.BUILDER);

        private final String id;
        private final EssentialPermission permission;

        Channel(String id) {
            this.id = id;
            this.permission = null;
        }

        Channel(String id, EssentialPermission permission) {
            this.id = id;
            this.permission = permission;
        }

        @Nullable
        public static Channel getById(String id) {
            for (Channel value : values()) {
                if (value.id.equalsIgnoreCase(id)) {
                    return value;
                }
            }

            return null;
        }

        public String getId() {
            return this.id;
        }

        public void send(final Component message, final Set<UUID> pingedUsers, final Predicate<OnlineUser> shouldSend, final ChatType messageType, final UUID author) {
            final boolean playSound = KiloConfig.main().chat().ping().pingSound().enabled;
            for (OnlineUser user : KiloEssentials.getUserManager().getOnlineUsersAsList()) {
                if (!shouldSend.test(user)) continue;
                if (this.permission == null || user.hasPermission(this.permission)) {
                    final boolean mentioned = pingedUsers.contains(user.getUuid());
                    if (mentioned && playSound) {
                        ServerChat.pingUser(user, MentionTypes.PUBLIC);
                    }
                    final boolean mentionOnly = user.getPreference(Preferences.CHAT_VISIBILITY) == VisibilityPreference.MENTIONS;
                    if (mentionOnly && !mentioned && messageType == ChatType.CHAT) {
                        continue;
                    }
                    user.asPlayer().sendMessage(message, messageType, author);
                }
            }
        }

        public void send(Component message, ChatType messageType, UUID uuid) {
            this.send(message, new HashSet<>(), (user) -> true, messageType, uuid);
        }

        public void send(Component message) {
            this.send(message, ChatType.SYSTEM, Util.NIL_UUID);
        }

        public String getFormat() {
            final ChatFormatsConfigSection formats = KiloConfig.main().chat().prefixes();
            return switch (this) {
                case PUBLIC -> formats.publicChat;
                case STAFF -> formats.staffChat;
                case BUILDER -> formats.builderChat;
            };

        }
    }

    public enum MentionTypes {
        PUBLIC,
        PRIVATE
    }

    public enum VisibilityPreference {
        ALL,
        MENTIONS;

        @Nullable
        public static VisibilityPreference getByName(final String name) {
            for (VisibilityPreference value : values()) {
                if (value.name().equalsIgnoreCase(name)) {
                    return value;
                }
            }

            return null;
        }

        public static String[] names() {
            String[] strings = new String[values().length];
            for (int i = 0; i < values().length; i++) {
                strings[i] = values()[i].toString();
            }
            return strings;
        }

        @Override
        public String toString() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

}
