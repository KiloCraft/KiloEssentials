package org.kilocraft.essentials.chat;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.netty.channel.Channel;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.PlaySoundIdS2CPacket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.EssentialPermission;
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
import org.kilocraft.essentials.util.Texter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ServerChat {
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
    private static String logFormat = ModConstants.translation("channel.message.logged");
    private static String urlHoverStyle = ModConstants.translation("channel.message.hover.url");

    private static boolean pingSoundEnabled;
    private static boolean pingEnabled;

    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
    private static final String ITEM_FORMATTED = "<I>";
    private static final Pattern LINK_PATTERN = Pattern.compile(RegexLib.URL.get());
    private static final int LINK_MAX_LENGTH = 20;

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

    public static void send(final OnlineUser sender, final TextMessage message, Channel channel) {
        message.setMessage(message.getOriginal(), KiloEssentials.hasPermissionNode(sender.getCommandSource(), EssentialPermission.CHAT_COLOR));

        TextMessage prefix = new TextMessage(ConfigVariableFactory.replaceUserVariables(channel.getPrefix(), sender)
                .replace("%USER_RANKED_DISPLAYNAME%", sender.getRankedDisplayName().asFormattedString()));

        processPings(sender, message, channel);
        message.setMessage(message.getFormattedMessage());

        Text component = message.toComponent();

        Matcher matcher = LINK_PATTERN.matcher(message.getOriginal());
        if (sender.hasPermission(EssentialPermission.CHAT_URL) && matcher.find()) {
            String url = message.getOriginal().substring(matcher.start(), matcher.end());
            String[] strings = message.getOriginal().split(url);
            String shortenedUrl = url.substring(0, Math.min(url.length(), LINK_MAX_LENGTH));

            Text link = new LiteralText(shortenedUrl).styled((style) -> {
                style.setClickEvent(Texter.Events.onClickOpen(url));
                style.setHoverEvent(Texter.Events.onHover(urlHoverStyle));
            }).formatted(Formatting.AQUA, Formatting.ITALIC);

            if (url.length() > LINK_MAX_LENGTH) {
                link.append("...");
                link.append(url.substring(url.length() - 4));
            }

            component = new LiteralText("")
                    .append(strings[0])
                    .append(link);

            if (strings.length > 1) {
                component.append(strings[1]);
            }
        }

        if (message.getOriginal().contains(itemFormat)) {
            ServerPlayerEntity player = sender.getPlayer();
            ItemStack itemStack = player.getMainHandStack();

            String[] strings = message.getOriginal().replace(itemFormat, ITEM_FORMATTED).split(ITEM_FORMATTED);

            component = new LiteralText("")
                    .append(strings[0].replace("[", ""))
                    .append(itemStack.toHoverableText());

            if (strings.length > 1) {
                component.append(strings[1].replace("]", ""));
            }
        }

        Text text = new LiteralText("");
        text.append(
                prefix.toComponent()
                        .styled((style) -> {
                            style.setHoverEvent(hoverEvent(sender, channel));
                            style.setClickEvent(clickEvent(sender));
                        })
        ).append(" ").append(component);

        KiloServer.getServer().sendMessage(String.format(logFormat, channel.getId(), sender.getUsername(), component.asFormattedString()));
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
                    ServerChat.pingPlayer(user.getPlayer(), PingType.EVERYONE);
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
                pingPlayer(target.getPlayer(), PingType.PUBLIC);
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

    private static void pingPlayer(final ServerPlayerEntity target, final PingType type) {
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

        target.networkHandler.sendPacket(new PlaySoundIdS2CPacket(new Identifier(soundId), SoundCategory.MASTER, vec3d, volume, pitch));
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
            if (!src.isConsole() && src.isOnline() &&  !((ServerUser) src.getUser()).isStaff()) {
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
            throw ServerChat.SAME_TARGETS_EXCEPTION.create();
        }

        ServerChat.sendPrivateMessage(source, user, message);
        return 1;
    }

    public static void sendPrivateMessage(final ServerCommandSource source, final OnlineUser target, final String message) throws CommandSyntaxException {
        final String format = ServerChat.config.privateChat().privateChat;
        final String me_format = ServerChat.config.privateChat().privateChatMeFormat;
        final String sourceName = source.getName();

        Map<String, UUID> ignoreList =  target.getSetting(Settings.IGNORE_LIST);
        if (CommandUtils.isPlayer(source) && ignoreList.containsValue(source.getPlayer().getUuid())) {
            throw ServerChat.CANT_MESSAGE_EXCEPTION.create();
        }

        final String toSource = format.replace("%SOURCE%", me_format)
                .replace("%TARGET%", "&r" + target.getUsername() + "&r")
                .replace("%MESSAGE%", message);
        final String toTarget = format.replace("%SOURCE%", sourceName)
                .replace("%TARGET%", me_format)
                .replace("%MESSAGE%", message);

        final String toSpy = format.replace("%SOURCE%", sourceName)
                .replace("%TARGET%", target.getUsername() + "&r")
                .replace("%MESSAGE%", message);

        KiloChat.sendMessageToSource(source, new LiteralText(
                new TextMessage(toSource, true).getFormattedMessage()).formatted(Formatting.WHITE));
        KiloChat.sendMessageTo(target.getPlayer(), new LiteralText(
                new TextMessage(toTarget, true).getFormattedMessage()).formatted(Formatting.WHITE));

        for (final OnlineServerUser user : KiloServer.getServer().getUserManager().getOnlineUsers().values()) {
            if (user.getSetting(Settings.SOCIAL_SPY) && !CommandUtils.areTheSame(source, user) && !CommandUtils.areTheSame(target, user))
                KiloChat.sendMessageTo(user.getPlayer(), new LiteralText(
                    new TextMessage(toSpy, true).getFormattedMessage()).formatted(Formatting.WHITE));
        }

        KiloServer.getServer().sendMessage(String.format("[Chat/Private] %s -> %s: %s", source.getName(), target.getUsername(), message));
    }

    public static void sendCommandSpy(final ServerCommandSource source, final String message) {
        final String format = ServerChat.config.commandSpyFormat;
        final String toSpy = format.replace("%SOURCE%", source.getName())
                .replace("%MESSAGE%",  message);

        for (final OnlineServerUser user : KiloServer.getServer().getUserManager().getOnlineUsers().values()) {
            if (user.getSetting(Settings.COMMAND_SPY) && !CommandUtils.areTheSame(source, user)) {
                KiloChat.sendMessageTo(user.getPlayer(), new LiteralText(
                        new TextMessage(toSpy, true).getFormattedMessage()).formatted(Formatting.GRAY));
            }
        }
    }

    public static void sendToStaff(Text message) {
        for (OnlineUser user : KiloServer.getServer().getUserManager().getOnlineUsersAsList()) {
            if (user.hasPermission(EssentialPermission.STAFF)) {
                user.sendMessage(message);
            }
        }
    }

    public static void sendToBuilders(Text message) {
        for (OnlineUser user : KiloServer.getServer().getUserManager().getOnlineUsersAsList()) {
            if (user.hasPermission(EssentialPermission.BUILDER)) {
                user.sendMessage(message);
            }
        }
    }

    private static final SimpleCommandExceptionType SAME_TARGETS_EXCEPTION = new SimpleCommandExceptionType(new LiteralText("You can't message your self!"));
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
                    ServerChat.sendToStaff(message);
                    break;
                case BUILDER:
                    ServerChat.sendToBuilders(message);
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

            return "! <%USER_RANKED_DISPLAYNAME%> ";
        }
    }

    private enum PingType {
        PUBLIC,
        PRIVATE,
        EVERYONE;
    }
}
