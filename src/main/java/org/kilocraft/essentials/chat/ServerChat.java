package org.kilocraft.essentials.chat;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.PlaySoundIdS2CPacket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.EssentialPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.commands.CommandUtils;
import org.kilocraft.essentials.config.ConfigVariableFactory;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.config.main.sections.chat.ChatConfigSection;
import org.kilocraft.essentials.config.main.sections.chat.ChatPingSoundConfigSection;
import org.kilocraft.essentials.user.OnlineServerUser;
import org.kilocraft.essentials.user.ServerUser;
import org.kilocraft.essentials.user.setting.Settings;
import org.kilocraft.essentials.util.RegexLib;
import org.kilocraft.essentials.util.text.Texter;
import org.kilocraft.essentials.util.messages.nodes.ExceptionMessageNode;

import java.rmi.UnexpectedException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ServerChat {
    private static final String DEBUG_EXCEPTION = "--texc";
    private static ChatConfigSection config;
    private static String pingEveryoneTemplate;
    private static String senderFormat;
    private static String displayFormat;
    private static String pingFailedDisplayFormat;
    private static String everyoneDisplayFormat;
    private static String itemFormat;

    private static String hoverStyle = ModConstants.translation("channel.message.hover");
    private static String hoverStyleNicked = ModConstants.translation("channel.message.hover.nicked");
    private static String hoverDateStyle = ModConstants.translation("channel.message.hover.time");
    private static String commandSpyHoverStyle = ModConstants.translation("channel.commandspy.hover");

    private static boolean pingSoundEnabled;
    private static boolean pingEnabled;

    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
    private static final Pattern LINK_PATTERN = Pattern.compile(RegexLib.URL.get());
    private static final int LINK_MAX_LENGTH = 20;
    private static final int COMMAND_MAX_LENGTH = 80;

    public static void load() {
        config = KiloConfig.main().chat();
        pingEveryoneTemplate = ServerChat.config.ping().everyoneTypedFormat;
        senderFormat = ServerChat.config.ping().typedFormat;
        displayFormat = ServerChat.config.ping().pingedFormat;
        pingFailedDisplayFormat = ServerChat.config.ping().pingedNotPingedFormat;
        everyoneDisplayFormat = ServerChat.config.ping().everyonePingedFormat;
        itemFormat = ServerChat.config.itemFormat;
        pingSoundEnabled = ServerChat.config.ping().pingSound().enabled;
        pingEnabled = ServerChat.config.ping().enabled;
    }

    public static void sendSafely(final OnlineUser sender, final TextMessage message, final Channel channel) {
        try {
            send(sender, message, channel);
        } catch (Exception e) {
            sender.getCommandSource().sendError(
                    Texter.toText("an unexpected exception occurred while processing the message")
                            .append("\n").append(Util.getInnermostMessage(e))
            );

            KiloEssentials.getLogger().error("Processing a chat message throw an exception", e);
        }
    }

    public static void send(final OnlineUser sender, final TextMessage message, final Channel channel) throws Exception {
        if (message.getOriginal().startsWith(DEBUG_EXCEPTION)) {
            throw new UnexpectedException("Debug exception thrown by " + sender.getUsername() + message.getOriginal().replaceFirst(DEBUG_EXCEPTION, ""));
        }

        boolean processWords;
        if (channel == Channel.PUBLIC) {
            processWords = true;
        } else {
            processWords = KiloConfig.messages().censorList().censorPrivateChannels;
        }

        try {
            message.setMessage(
                    processWords ? processWords(sender, message.getOriginal()) : message.getOriginal(),
                    KiloEssentials.hasPermissionNode(sender.getCommandSource(), EssentialPermission.CHAT_COLOR)
            );
        } catch (Exception e) {
            return;
        }

        TextMessage prefix = new TextMessage(ConfigVariableFactory.replaceUserVariables(channel.getPrefix(), sender)
                .replace("%USER_RANKED_DISPLAYNAME%", sender.getRankedDisplayName().asFormattedString()));

        processPings(sender, message, channel);

        Text component = ServerChat.stringToMessageComponent(
                message.getFormattedMessage(),
                sender,
                sender.hasPermission(EssentialPermission.CHAT_URL),
                sender.hasPermission(EssentialPermission.CHAT_SHOW_ITEM)
        );

        Text text = new LiteralText("");
        text.append(
                prefix.toComponent()
                        .styled((style) -> {
                            style.setHoverEvent(hoverEvent(sender, channel));
                            style.setClickEvent(clickEvent(sender));
                        })
        ).append(" ").append(component);

        KiloServer.getServer().sendMessage(text.getString());
        channel.send(text);
    }

    private static void processPings(final OnlineUser sender, final TextMessage message, final Channel channel) {
        if (!pingEnabled && !KiloEssentials.hasPermissionNode(sender.getCommandSource(), EssentialPermission.CHAT_PING_OTHER)) {
            return;
        }

        if (message.getOriginal().contains(pingEveryoneTemplate) && KiloEssentials.hasPermissionNode(sender.getCommandSource(), EssentialPermission.CHAT_PING_EVERYONE)) {
            message.setMessage(message.getFormattedMessage().replaceAll(pingEveryoneTemplate, everyoneDisplayFormat + "&r"));

            for (OnlineUser user : KiloServer.getServer().getUserManager().getOnlineUsersAsList()) {
                if (user.getSetting(Settings.CHAT_CHANNEL) == channel) {
                    ServerChat.pingPlayer(user.asPlayer(), PingType.EVERYONE);
                }
            }
        }

        for (OnlineUser target : KiloServer.getServer().getUserManager().getOnlineUsersAsList()) {
            String format = senderFormat.replace("%PLAYER_NAME%", target.getUsername());
            if (!message.getOriginal().contains(format) || !KiloEssentials.hasPermissionNode(target.getCommandSource(), EssentialPermission.CHAT_GET_PINGED)) {
                continue;
            }

            boolean canPing = target.getSetting(Settings.CHAT_CHANNEL) == channel;
            String formattedPing = canPing ? displayFormat : pingFailedDisplayFormat;

            message.setMessage(
                    message.getFormattedMessage().replaceAll(
                            format,
                            formattedPing.replaceAll("%PLAYER_DISPLAYNAME%", target.getFormattedDisplayName() + "&r")
                    )
            );

            if (pingSoundEnabled && canPing) {
                pingPlayer(target.asPlayer(), PingType.PUBLIC);
            }
        }
    }

    private static HoverEvent hoverEvent(final OnlineUser user, Channel channel) {
        String date = String.format(hoverDateStyle, dateFormat.format(new Date()));

        if (user.hasNickname() && channel.getPrefix().contains("%USER_RANKED_DISPLAYNAME%")) {
            return Texter.Events.onHover(String.format(hoverStyleNicked, user.getUsername(), date));
        } else {
            return Texter.Events.onHover(String.format(hoverStyle, date));
        }
    }

    private static ClickEvent clickEvent(final OnlineUser user) {
        return Texter.Events.onClickSuggest("/msg " + user.getUsername() + " ");
    }

    public static void pingPlayer(final ServerPlayerEntity target, final PingType type) {
        ChatPingSoundConfigSection cfg = null;
        switch (type) {
            case PUBLIC:
                cfg = config.ping().pingSound();
                break;
            case PRIVATE:
                cfg = config.privateChat().pingSound();
                break;
            case EVERYONE:
                config.ping().pingSound();
                break;
        }

        if (cfg == null) {
            return;
        }

        Vec3d vec3d = target.getCommandSource().getPosition();
        String soundId = cfg.id;
        float volume = (float) cfg.volume;
        float pitch = (float) cfg.pitch;

        if (target.networkHandler != null) {
            target.networkHandler.sendPacket(new PlaySoundIdS2CPacket(new Identifier(soundId), SoundCategory.MASTER, vec3d, volume, pitch));
        }
    }

    public static void addSocialSpy(final ServerPlayerEntity player) {
        KiloServer.getServer().getOnlineUser(player).getSettings().set(Settings.SOCIAL_SPY, true);
    }

    public static void removeSocialSpy(final ServerPlayerEntity player) {
        KiloServer.getServer().getOnlineUser(player).getSettings().set(Settings.SOCIAL_SPY, false);
    }

    public static boolean isSocialSpy(final ServerPlayerEntity player) {
        return KiloServer.getServer().getOnlineUser(player).getSetting(Settings.SOCIAL_SPY);
    }

    public static void addCommandSpy(final ServerPlayerEntity player) {
        KiloServer.getServer().getOnlineUser(player).getSettings().set(Settings.COMMAND_SPY, true);
    }

    public static void removeCommandSpy(final ServerPlayerEntity player) {
        KiloServer.getServer().getOnlineUser(player).getSettings().set(Settings.COMMAND_SPY, false);
    }

    public static boolean isCommandSpy(final ServerPlayerEntity player) {
        return KiloServer.getServer().getOnlineUser(player).getSetting(Settings.COMMAND_SPY);
    }

    public static int sendDirectMessage(final ServerCommandSource source, final ServerPlayerEntity target, final String message) throws CommandSyntaxException {
        final OnlineUser user = KiloServer.getServer().getOnlineUser(target);
        final CommandSourceUser src = KiloServer.getServer().getCommandSourceUser(source);

        if (!((ServerUser) user).shouldMessage() && src.getUser() != null) {
            if (!src.isConsole() && src.isOnline() && !((ServerUser) src.getUser()).isStaff()) {
                throw ServerChat.CANT_MESSAGE_EXCEPTION.create();
            }
        }

        if  (!CommandUtils.isConsole(source)) {
            final OnlineUser online = KiloServer.getServer().getOnlineUser(source.getPlayer());
            user.setLastMessageSender(source.getPlayer().getUuid());
            online.setLastMessageSender(target.getUuid());
            online.setLastPrivateMessage(message);
        }

        if (target == null) {
            throw ServerChat.TARGET_OFFLINE_EXCEPTION.create();
        }

        if (CommandUtils.areTheSame(source, target)) {
            throw KiloCommands.getException(ExceptionMessageNode.SOURCE_IS_TARGET).create();
        }

        String msg = message;
        if (KiloConfig.messages().censorList().censorDirectMessages) {
            try {
                msg = processWords(src, msg);
            } catch (Exception e) {
                return -1;
            }
        }

        ServerChat.messagePrivately(source, user, msg);
        return 1;
    }

    public static void messagePrivately(final ServerCommandSource source, final OnlineUser target, final String message) throws CommandSyntaxException {
        String format = ServerChat.config.privateChat().privateChat;
        String me_format = ServerChat.config.privateChat().privateChatMeFormat;
        String sourceName = source.getName();

        if (CommandUtils.isPlayer(source) && target.ignored(source.getPlayer().getUuid())) {
            throw KiloCommands.getException(ExceptionMessageNode.IGNORED, target.getFormattedDisplayName()).create();
        }

        String toSource = format.replace("%SOURCE%", me_format)
                .replace("%TARGET%", "&r" + target.getUsername() + "&r")
                .replace("%MESSAGE%", message);
        String toTarget = format.replace("%SOURCE%", sourceName)
                .replace("%TARGET%", me_format)
                .replace("%MESSAGE%", message);

        String toSpy = format.replace("%SOURCE%", sourceName)
                .replace("%TARGET%", target.getUsername() + "&r")
                .replace("%MESSAGE%", message);

        if (target.getSetting(Settings.SOUNDS)) {
            pingPlayer(target.asPlayer(), PingType.PRIVATE);
        }

        KiloChat.sendMessageToSource(source, new TextMessage(toSource, true).toComponent().formatted(Formatting.WHITE));
        KiloChat.sendMessageTo(target.asPlayer(), new TextMessage(toTarget, true).toComponent().formatted(Formatting.WHITE));

        for (final OnlineServerUser user : KiloServer.getServer().getUserManager().getOnlineUsers().values()) {
            if (user.getSetting(Settings.SOCIAL_SPY) && !CommandUtils.areTheSame(source, user) && !CommandUtils.areTheSame(target, user)) {
                KiloChat.sendMessageTo(user.asPlayer(), new LiteralText(new TextMessage(toSpy, true).getFormattedMessage()).formatted(Formatting.WHITE));
            }
        }

        KiloServer.getServer().sendMessage(toSpy);
    }

    public static void sendCommandSpy(final ServerCommandSource source, final String command) {
        String format = ServerChat.config.commandSpyFormat;
        String shortenedCommand = command.substring(0, Math.min(command.length(), COMMAND_MAX_LENGTH));
        String toSpy = format.replace("%SOURCE%", source.getName()).replace("%COMMAND%",  shortenedCommand);
        Text text = Texter.toText(toSpy).formatted(Formatting.GRAY);

        if (command.length() > COMMAND_MAX_LENGTH) {
            text.append("...");
        }

        text.styled((style) -> {
            style.setHoverEvent(Texter.Events.onHover(commandSpyHoverStyle));
            style.setClickEvent(Texter.Events.onClickSuggest(command));
        });

        for (OnlineServerUser user : KiloServer.getServer().getUserManager().getOnlineUsers().values()) {
            if (user.getSetting(Settings.COMMAND_SPY) && !CommandUtils.areTheSame(source, user)) {
                user.sendMessage(text);
            }
        }
    }

    public static void send(Text message, EssentialPermission permission) {
        for (OnlineUser user : KiloServer.getServer().getUserManager().getOnlineUsersAsList()) {
            if (user.hasPermission(permission)) {
                user.sendMessage(message);
            }
        }
    }

    private static String processWords(@NotNull final OnlineUser sender, @NotNull final String message) throws Exception {
        String msg = message;
        String lowerCased = msg.toLowerCase(Locale.ROOT);
        int index = 0;
        boolean censor = KiloConfig.messages().censorList().censor;

        for (String value : KiloConfig.messages().censorList().words) {
            String s = value.toLowerCase(Locale.ROOT);
            if (lowerCased.contains(s)) {
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < s.length(); i++) {
                    builder.append(KiloConfig.messages().censorList().alternateChar);
                }

                msg = msg.replaceAll(("(?i)" + s), Matcher.quoteReplacement(builder.toString()));
                index++;

                if (!censor) {
                    break;
                }
            }
        }

        if (index >= 1 && !censor) {
            sender.sendError(String.format(KiloConfig.messages().censorList().blockMessage, sender.getFormattedDisplayName()));
            throw new Exception(KiloConfig.messages().censorList().blockMessage);
        }

        return msg;
    }

    public static Text stringToMessageComponent(@NotNull String string, OnlineUser sender, boolean appendLinks, boolean appendItems) {
        Validate.notNull(string, "Message string must not be null!");
        Text text = new LiteralText("");
        String[] strings = string.split(" ");

        int i = 0;
        for (String s : strings) {
            i++;
            Matcher matcher = LINK_PATTERN.matcher(string);
            if (appendLinks && matcher.find()) {
                String shortenedUrl = s.substring(0, Math.min(s.length(), LINK_MAX_LENGTH));

                Text link = new LiteralText(shortenedUrl).styled((style) -> style.setClickEvent(Texter.Events.onClickOpen(s)));

                if (s.length() > LINK_MAX_LENGTH) {
                    link.append("...");
                    link.append(s.substring(s.length() - 5));
                }

                text.append(link);
            } else if (appendItems && s.contains(itemFormat)) {
                ServerPlayerEntity player = sender.asPlayer();
                ItemStack itemStack = player.getMainHandStack();

                Text item = new LiteralText("").append(itemStack.toHoverableText());
                text.append(item);
            } else {
                text.append(s);
            }

            if (i < strings.length) {
                text.append(" ");
            }
        }

        return text;
    }

    private static final SimpleCommandExceptionType TARGET_OFFLINE_EXCEPTION = new SimpleCommandExceptionType(new LiteralText("The Target player is offline!"));
    private static final SimpleCommandExceptionType CANT_MESSAGE_EXCEPTION = new  SimpleCommandExceptionType(LangText.getFormatter(true, "command.message.error"));

    public enum Channel {
        PUBLIC("public"),
        STAFF("staff"),
        BUILDER("builder");

        private String id;
        Channel(String id) {
            this.id = id;
        }

        public String getId() {
            return this.id;
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

        public void send(Text message) {
            switch (this) {
                case PUBLIC:
                    KiloChat.broadCast(message);
                    break;
                case STAFF:
                    ServerChat.send(message, EssentialPermission.STAFF);
                    break;
                case BUILDER:
                    ServerChat.send(message, EssentialPermission.BUILDER);
                    break;
            }
        }

        public String getPrefix() {
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

    public enum PingType {
        PUBLIC,
        PRIVATE,
        EVERYONE;
    }
}
