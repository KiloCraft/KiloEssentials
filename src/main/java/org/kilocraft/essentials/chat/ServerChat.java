package org.kilocraft.essentials.chat;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.packet.s2c.play.PlaySoundIdS2CPacket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.EssentialPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.event.player.PlayerOnDirectMessageEvent;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.chat.ParseResult;
import org.kilocraft.essentials.commands.CommandUtils;
import org.kilocraft.essentials.config.ConfigVariableFactory;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.config.main.sections.chat.ChatConfigSection;
import org.kilocraft.essentials.config.main.sections.chat.ChatPingSoundConfigSection;
import org.kilocraft.essentials.events.player.PlayerOnChatMessageEventImpl;
import org.kilocraft.essentials.events.player.PlayerOnDirectMessageEventImpl;
import org.kilocraft.essentials.user.OnlineServerUser;
import org.kilocraft.essentials.user.ServerUser;
import org.kilocraft.essentials.user.ServerUserManager;
import org.kilocraft.essentials.user.preference.Preferences;
import org.kilocraft.essentials.util.RegexLib;
import org.kilocraft.essentials.util.messages.nodes.ExceptionMessageNode;
import org.kilocraft.essentials.util.registry.RegistryUtils;
import org.kilocraft.essentials.util.text.Texter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ServerChat {
    private static final String DEBUG_EXCEPTION = "--texc";
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    private static final Pattern LINK_PATTERN = Pattern.compile(RegexLib.URL.get());
    private static final int LINK_MAX_LENGTH = 20;
    private static final int COMMAND_MAX_LENGTH = 45;
    private static final SimpleCommandExceptionType CANT_MESSAGE_EXCEPTION = new SimpleCommandExceptionType(StringText.of(true, "command.message.error"));
    private static ChatConfigSection config;
    private static String pingEveryoneTemplate;
    private static Pattern pingEveryonePattern;
    private static String senderFormat;
    private static String displayFormat;
    private static String pingFailedDisplayFormat;
    private static String everyoneDisplayFormat;
    private static String itemFormat;
    private static Pattern itemPattern;
    private static String hoverStyle = ModConstants.translation("channel.message.hover");
    private static String hoverStyleNicked = ModConstants.translation("channel.message.hover.nicked");
    private static String hoverDateStyle = ModConstants.translation("channel.message.hover.time");
    private static String commandSpyHoverStyle = ModConstants.translation("channel.commandspy.hover");
    private static boolean pingSoundEnabled;
    private static boolean pingEnabled;

    public static void load() {
        config = KiloConfig.main().chat();
        pingEveryoneTemplate = ServerChat.config.ping().everyoneTypedFormat;
        pingEveryonePattern = Pattern.compile(pingEveryoneTemplate);
        senderFormat = ServerChat.config.ping().typedFormat;
        displayFormat = ServerChat.config.ping().pingedFormat;
        pingFailedDisplayFormat = ServerChat.config.ping().pingedNotPingedFormat;
        everyoneDisplayFormat = ServerChat.config.ping().everyonePingedFormat;
        itemFormat = ServerChat.config.itemFormat;
        itemPattern = Pattern.compile(itemFormat);
        pingSoundEnabled = ServerChat.config.ping().pingSound().enabled;
        pingEnabled = ServerChat.config.ping().enabled;
    }

    public static void sendChatMessage(final OnlineUser user, final String raw, final Channel channel) {
        KiloServer.getServer().triggerEvent(new PlayerOnChatMessageEventImpl(user.asPlayer(), raw, channel));
        TextComponent.Builder text = Component.text();
        text.append(ComponentText.of(ConfigVariableFactory.replaceUserVariables(channel.getFormat(), user))
                .style(style -> style.hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(hoverEvent(user, channel)))
                .clickEvent(net.kyori.adventure.text.event.ClickEvent.suggestCommand("/msg " + user.getUsername() + " "))));
        String[] parts = raw.split(" ");

        //reorder list entries, so special cases (pings, censor, item)
        List<String> ordered = new ArrayList<>();
        boolean b = false;
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            Matcher itemMatcher = itemPattern.matcher(part);
            if (i == 0) {
                ordered.add(part);
                continue;
            }
            if (part.matches(RegexLib.URL.get()) || itemMatcher.find() || shouldPing(user, part)) {
                ordered.add(part);
                b = true;
            } else {
                int last = ordered.size() - 1;
                if (!b) ordered.set(last, ordered.get(last) + " " + part);
                else {
                    ordered.add(part);
                    b = false;
                }
            }
        }
        for (String part : ordered) {
            ParseResult result = parse(user, part, channel);
            text.append(result.getResult()).append(Component.text(" "));
        }

        if (user.getPreference(Preferences.CHAT_VISIBILITY) == VisibilityPreference.MENTIONS) {
            user.sendMessage(text.build());
        }

        channel.send(ComponentText.toText(text.build()), new ArrayList<>());
        KiloServer.getServer().sendMessage(text.build());
    }

    private static boolean shouldPing(OnlineUser sender, String message) {
        if (message.contains(pingEveryoneTemplate) && KiloEssentials.hasPermissionNode(sender.getCommandSource(), EssentialPermission.CHAT_PING_EVERYONE)) {
            return true;
        }
        for (OnlineUser user : KiloServer.getServer().getUserManager().getOnlineUsersAsList()) {
            String nameFormat = senderFormat.replace("%PLAYER_NAME%", user.getUsername());
            String nickFormat = senderFormat.replace("%PLAYER_NAME%", ComponentText.clearFormatting(user.getDisplayName()));
            if ((message.contains(nameFormat) || message.contains(nickFormat)) && KiloEssentials.hasPermissionNode(user.getCommandSource(), EssentialPermission.CHAT_GET_PINGED)) {
                return true;
            }
        }
        return false;
    }

    private static ParseResult parse(OnlineUser sender, String input, final Channel channel) {
        boolean processWords;
        if (channel == Channel.PUBLIC) {
            processWords = true;
        } else {
            processWords = KiloConfig.messages().censorList().censorPrivateChannels;
        }
        input = processWords ? processWord(input) : input;
        ParseResult result = new ParseResult(input);

        Matcher itemMatcher = itemPattern.matcher(input);
        TextComponent.Builder text = Component.text();
        if (input.matches(RegexLib.URL.get())) {
            String shortenedUrl = input.substring(0, Math.min(input.length(), LINK_MAX_LENGTH));
            if (input.length() > LINK_MAX_LENGTH) {
                text.content(shortenedUrl + "..." + input.substring(input.length() - 5) + " ");
            } else {
                text.append(ComponentText.of(input + " "));
            }
            String finalInput = input;
            text.style(style -> style.clickEvent(net.kyori.adventure.text.event.ClickEvent.openUrl(finalInput)).hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(Component.text("Open URL!").color(NamedTextColor.AQUA))));
            result.setType(ParseResult.ParseType.LINK).setResult(text.build());
        } else if (itemMatcher.find()) {
            ServerPlayerEntity player = sender.asPlayer();
            ItemStack itemStack = player.getMainHandStack();
            CompoundTag tag = itemStack.getTag();
            text.append(ComponentText.of(input.substring(0, Math.max(0, itemMatcher.start())))).append(Component.text("[")).append(ComponentText.toComponent(itemStack.getName())).append(Component.text("]")).append(ComponentText.of(input.substring(Math.min(itemMatcher.end(), input.length()))));
            text.style(style -> style.hoverEvent(net.kyori.adventure.text.event.HoverEvent.showItem(Key.key(RegistryUtils.toIdentifier(itemStack.getItem())), 1, BinaryTagHolder.of(tag == null ? new CompoundTag().toString() : tag.toString()))));
            result.setType(ParseResult.ParseType.ITEM).setResult(text.build());
        } else {
            processPing(sender, result, channel);
        }
        return result;
    }

    private static void processPing(final OnlineUser sender, ParseResult result, final Channel channel) {
        if (!pingEnabled && !KiloEssentials.hasPermissionNode(sender.getCommandSource(), EssentialPermission.CHAT_PING_OTHER)) {
            return;
        }
        String message = result.getInput();
        if (message.contains(pingEveryoneTemplate) && KiloEssentials.hasPermissionNode(sender.getCommandSource(), EssentialPermission.CHAT_PING_EVERYONE)) {
            message = message.replaceAll(pingEveryoneTemplate, everyoneDisplayFormat + "<reset>");
            for (OnlineUser user : KiloServer.getServer().getUserManager().getOnlineUsersAsList()) {
                if (user.getPreference(Preferences.CHAT_CHANNEL) == channel) {
                    result.addPinged(user);
                    ServerChat.pingUser(user.asPlayer(), config.ping().pingSound());
                }
            }
        }

        for (OnlineUser user : KiloServer.getServer().getUserManager().getOnlineUsersAsList()) {
            String nameFormat = senderFormat.replace("%PLAYER_NAME%", user.getUsername());
            String nickFormat = senderFormat.replace("%PLAYER_NAME%", ComponentText.clearFormatting(user.getDisplayName()));
            if ((!message.contains(nameFormat) && !message.contains(nickFormat)) || !KiloEssentials.hasPermissionNode(user.getCommandSource(), EssentialPermission.CHAT_GET_PINGED)) {
                continue;
            }

            boolean canPing = user.getPreference(Preferences.CHAT_CHANNEL) == channel;
            String format = message.contains(nameFormat) ? nameFormat : nickFormat;
            String formattedPing = canPing ? displayFormat : pingFailedDisplayFormat;
            message = message.replaceAll(format, formattedPing.replaceAll("%PLAYER_DISPLAYNAME%", user.getFormattedDisplayName()) + "<reset>");

            if (pingSoundEnabled && canPing) {
                result.addPinged(user);
                pingUser(user, MentionTypes.PUBLIC);
            }
        }
        result.setResult(ComponentText.of(message));
    }

    private static Component hoverEvent(final OnlineUser user, Channel channel) {
        String date = String.format(hoverDateStyle, dateFormat.format(new Date()));

        assert channel.getFormat() != null;
        if (user.hasNickname()) {
            return ComponentText.of(String.format(hoverStyleNicked, user.getUsername(), date));
        } else {
            return ComponentText.of(String.format(hoverStyle, date));
        }
    }

    public static void pingUser(final OnlineUser target, final MentionTypes type) {
        if (target.getPreference(Preferences.DON_NOT_DISTURB) && type != MentionTypes.EVERYONE) {
            return;
        }

        ChatPingSoundConfigSection cfg = null;
        switch (type) {
            case PUBLIC:
            case EVERYONE:
                cfg = config.ping().pingSound();
                break;
            case PRIVATE:
                cfg = config.privateChat().pingSound();
                break;
        }

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
        CommandSourceUser src = KiloServer.getServer().getCommandSourceUser(source);

        if (!((ServerUser) target).shouldMessage() && src.getUser() != null) {
            if (!src.isConsole() && src.isOnline() && !((ServerUser) src.getUser()).isStaff()) {
                throw ServerChat.CANT_MESSAGE_EXCEPTION.create();
            }
        }

        if (!CommandUtils.isConsole(source)) {
            OnlineUser online = KiloServer.getServer().getOnlineUser(source.getPlayer());
            online.setLastMessageReceptionist(target);
            target.setLastMessageReceptionist(online);
        }

        if (CommandUtils.areTheSame(source, target)) {
            throw KiloCommands.getException(ExceptionMessageNode.SOURCE_IS_TARGET).create();
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
            OnlineUser user = KiloServer.getServer().getOnlineUser(source.getPlayer());
            if (KiloServer.getServer().getUserManager().getPunishmentManager().isMuted(user)) {
                user.sendMessage(ServerUserManager.getMuteMessage(user));
                return;
            }

            if (target.ignored(source.getPlayer().getUuid())) {
                throw KiloCommands.getException(ExceptionMessageNode.IGNORED, target.getFormattedDisplayName()).create();
            }
        }

        PlayerOnDirectMessageEvent event = KiloServer.getServer().triggerEvent(new PlayerOnDirectMessageEventImpl(source, target, raw));
        if (event.isCancelled()) {
            if (event.getCancelReason() != null) {
                source.sendError(Texter.newText(event.getCancelReason()));
            }
            return;
        }
        final String message = event.getMessage();

        String toSource = format.replace("%SOURCE%", me_format)
                .replace("%TARGET%", "&r" + target.getUsername() + "&r")
                .replace("%MESSAGE%", message);
        String toTarget = format.replace("%SOURCE%", sourceName)
                .replace("%TARGET%", me_format)
                .replace("%MESSAGE%", message);

        String toSpy = ServerChat.config.socialSpyFormat.replace("%SOURCE%", sourceName)
                .replace("%TARGET%", target.getUsername() + "&r")
                .replace("%MESSAGE%", message);

        if (target.getPreference(Preferences.SOUNDS)) {
            pingUser(target, MentionTypes.PRIVATE);
        }

        ComponentText.toText(ComponentText.removeEvents(ComponentText.of(toSource)));

        KiloServer.getServer().getCommandSourceUser(source).sendMessage(toSource);
        target.sendMessage(toTarget);

        for (final OnlineServerUser user : KiloServer.getServer().getUserManager().getOnlineUsers().values()) {
            if (user.getPreference(Preferences.SOCIAL_SPY) && !CommandUtils.areTheSame(source, user) && !CommandUtils.areTheSame(target, user)) {
                user.sendMessage(toSpy);
            }

            KiloServer.getServer().triggerEvent(new PlayerOnDirectMessageEventImpl(source, target, raw));
        }

        KiloServer.getServer().sendMessage(toSpy);
    }

    public static void sendCommandSpy(final ServerCommandSource source, final String command) {
        String format = ServerChat.config.commandSpyFormat;
        String shortenedCommand = command.substring(0, Math.min(command.length(), COMMAND_MAX_LENGTH));
        String toSpy = format.replace("%SOURCE%", source.getName()).replace("%COMMAND%", shortenedCommand);
        MutableText text = Texter.newText(toSpy).formatted(Formatting.GRAY);

        if (command.length() > COMMAND_MAX_LENGTH) {
            text.append("...");
        }

        text.styled((style) -> style.withHoverEvent(Texter.Events.onHover(commandSpyHoverStyle)).withClickEvent(Texter.Events.onClickSuggest("/" + command)));

        for (OnlineServerUser user : KiloServer.getServer().getUserManager().getOnlineUsers().values()) {
            if (user.getPreference(Preferences.COMMAND_SPY) && !CommandUtils.areTheSame(source, user)) {
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

        private String id;

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

        public void send(Text message) {
            send(message, new ArrayList<>());
        }

        public void send(Text message, List<OnlineUser> mentioned) {
            switch (this) {
                case PUBLIC:
                    for (OnlineUser user : KiloServer.getServer().getUserManager().getOnlineUsersAsList()) {
                        VisibilityPreference preference = user.getPreference(Preferences.CHAT_VISIBILITY);
                        if (preference == VisibilityPreference.ALL || (preference == VisibilityPreference.MENTIONS && mentioned.contains(user))) {
                            user.sendMessage(message);
                        }
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
        EVERYONE;
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
