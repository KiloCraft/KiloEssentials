package org.kilocraft.essentials.chat;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.network.MessageType;
import net.minecraft.network.packet.s2c.play.PlaySoundIdS2CPacket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.config.main.sections.chat.ChatConfigSection;
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

import java.util.Locale;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.regex.Matcher;

public final class ServerChat {
    private static final int COMMAND_MAX_LENGTH = 45;
    private static final SimpleCommandExceptionType CANT_MESSAGE_EXCEPTION = new SimpleCommandExceptionType(StringText.of("command.message.error"));
    private static final ChatConfigSection config = KiloConfig.main().chat();

    public static void sendChatMessage(final OnlineUser user, final String raw, final Channel channel) {
        ChatMessage message = new ChatMessage(user, raw, channel);
        message.send();
    }

    public static void pingUser(final OnlineUser target, final MentionTypes type) {
        if (target.getPreference(Preferences.DON_NOT_DISTURB) && type != MentionTypes.EVERYONE) {
            return;
        }

        ChatPingSoundConfigSection cfg = switch (type) {
            case PUBLIC, EVERYONE -> config.ping().pingSound();
            case PRIVATE -> config.privateChat().pingSound();
        };

        pingUser(target.asPlayer(), cfg);
    }

    private static void pingUser(final ServerPlayerEntity target, final ChatPingSoundConfigSection cfg) {
        Vec3d vec3d = target.getCommandSource().getPosition();
        if (target.networkHandler != null) {
            target.networkHandler.sendPacket(
                    new PlaySoundIdS2CPacket(
                            new Identifier(cfg.id),
                            SoundCategory.MASTER,
                            vec3d, (float) cfg.volume, (float) cfg.pitch)
            );
        }
    }

    public static int sendDirectMessage(final ServerCommandSource source, final OnlineUser target, final String message) throws CommandSyntaxException {
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

        String msg = message;
        if (KiloConfig.messages().censorList().censorDirectMessages) {
            try {
                msg = processWords(msg);
            } catch (Exception e) {
                return -1;
            }
        }

        ServerChat.messagePrivately(source, target, msg);
        return 1;
    }

    public static void messagePrivately(final ServerCommandSource source, final OnlineUser target, final String raw) throws CommandSyntaxException {
        String format = ServerChat.config.privateChat().privateChat;
        String me_format = ServerChat.config.privateChat().privateChatMeFormat;
        String sourceName = source.getName();

        if (CommandUtils.isPlayer(source)) {
            OnlineUser user = KiloEssentials.getUserManager().getOnline(source);
            if (KiloEssentials.getUserManager().getPunishmentManager().isMuted(user)) {
                user.sendMessage(ServerUserManager.getMuteMessage(user));
                return;
            }

            if (target.ignored(source.getPlayer().getUuid())) {
                throw KiloCommands.getException("exception.ignored", target.getFormattedDisplayName()).create();
            }
        }

        String toSource = format.replace("%SOURCE%", me_format)
                .replace("%TARGET%", "&r" + target.getUsername() + "&r")
                .replace("%MESSAGE%", raw);
        String toTarget = format.replace("%SOURCE%", sourceName)
                .replace("%TARGET%", me_format)
                .replace("%MESSAGE%", raw);

        String toSpy = ServerChat.config.socialSpyFormat.replace("%SOURCE%", sourceName)
                .replace("%TARGET%", target.getUsername() + "&r")
                .replace("%MESSAGE%", raw);

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

    public static void sendCommandSpy(final ServerCommandSource source, final String command) {
        String format = ServerChat.config.commandSpyFormat;
        String shortenedCommand = command.substring(0, Math.min(command.length(), COMMAND_MAX_LENGTH));
        String toSpy = format.replace("%SOURCE%", source.getName()).replace("%COMMAND%", shortenedCommand);
        MutableText text = Texter.newText(toSpy).formatted(Formatting.GRAY);

        if (command.length() > COMMAND_MAX_LENGTH) {
            text.append("...");
        }

        text.styled((style) -> style.withHoverEvent(Texter.Events.onHover(ModConstants.translation("channel.commandspy.hover"))).withClickEvent(Texter.Events.onClickSuggest("/" + command)));

        for (OnlineServerUser user : KiloEssentials.getUserManager().getOnlineUsers().values()) {
            if (user.getPreference(Preferences.COMMAND_SPY) && !CommandUtils.areTheSame(source, user)) {
                user.sendMessage(text);
            }
        }
    }

    public static void send(Text message, EssentialPermission permission) {
        for (OnlineUser user : KiloEssentials.getUserManager().getOnlineUsersAsList()) {
            if (user.hasPermission(permission)) {
                user.sendMessage(message);
            }
        }
    }

    private static String processWords(@NotNull final String message) {
        String msg = message;
        String lowerCased = msg.toLowerCase(Locale.ROOT);

        for (String value : KiloConfig.messages().censorList().words) {
            String s = value.toLowerCase(Locale.ROOT);
            if (lowerCased.contains(s)) {
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < s.length(); i++) {
                    builder.append(KiloConfig.messages().censorList().alternateChar);
                }

                msg = msg.replaceAll(("(?i)" + s), Matcher.quoteReplacement(builder.toString()));
            }
        }


        return msg;
    }

    private static String processWord(@NotNull final String input) {
        String lowerCased = input.toLowerCase(Locale.ROOT);
        for (String value : KiloConfig.messages().censorList().words) {
            String s = value.toLowerCase(Locale.ROOT);
            if (lowerCased.contains(s)) {
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < s.length(); i++) {
                    builder.append(KiloConfig.messages().censorList().alternateChar);
                }
                return builder.toString();
            }
        }

        return input;
    }


    public enum Channel {
        PUBLIC("public"),
        STAFF("staff"),
        BUILDER("builder");

        private final String id;

        Channel(String id) {
            this.id = id;
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

        public void send(Text message, MessageType messageType, UUID uuid) {
            this.send(message, messageType, uuid, onlineUser -> true);
        }

        protected void send(Text message, MessageType messageType, UUID uuid, Predicate<OnlineUser> shouldSend) {
            switch (this) {
                case PUBLIC:
                    for (OnlineUser user : KiloEssentials.getUserManager().getOnlineUsersAsList()) {
                        if (shouldSend.test(user)) user.asPlayer().sendMessage(message, messageType, uuid);
                    }
                    break;
                case STAFF:
                    ServerChat.send(message, EssentialPermission.STAFF);
                    break;
                case BUILDER:
                    ServerChat.send(message, EssentialPermission.BUILDER);
                    break;
            }
        }

        public void send(Text message) {
            this.send(message, MessageType.SYSTEM, Util.NIL_UUID);
        }

        public String getFormat() {
            switch (this) {
                case PUBLIC:
                    return KiloConfig.main().chat().prefixes().publicChat;
                case STAFF:
                    return KiloConfig.main().chat().prefixes().staffChat;
                case BUILDER:
                    return KiloConfig.main().chat().prefixes().builderChat;
            }

            return null;
        }
    }

    public enum MentionTypes {
        PUBLIC,
        PRIVATE,
        EVERYONE
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
