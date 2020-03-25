package org.kilocraft.essentials.chat;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.network.packet.s2c.play.PlaySoundIdS2CPacket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.kilocraft.essentials.EssentialPermission;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.chat.ChatChannel;
import org.kilocraft.essentials.api.chat.LangText;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.commands.CommandUtils;
import org.kilocraft.essentials.config.ConfigVariableFactory;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.config.main.sections.chat.ChatConfigSection;
import org.kilocraft.essentials.user.OnlineServerUser;
import org.kilocraft.essentials.user.ServerUser;
import org.kilocraft.essentials.user.setting.Settings;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

public final class ServerChat {
    private static final ChatConfigSection config = KiloConfig.main().chat();
    private static final String everyone_template = ServerChat.config.ping().everyoneTypedFormat;
    private static final String senderFormat = ServerChat.config.ping().typedFormat;
    private static final String displayFormat = ServerChat.config.ping().pingedFormat;
    private static final String not_pinged_displayFormat = ServerChat.config.ping().pingedNotPingedFormat;
    private static final String everyone_displayFormat = ServerChat.config.ping().everyonePingedFormat;
    private static final boolean pingSoundEnabled = ServerChat.config.ping().pingSound().enabled;
    private static final boolean pingEnabled = ServerChat.config.ping().enabled;

    private ServerChat() {
    }

    public static void send(final OnlineUser sender, final String rawMessage, final ChatChannel channel) {
        final String template = KiloConfig.getMainNode().getNode("chat").getNode("channelsMeta").getNode(channel.getId() + "Chat").getString();
        final ServerPlayerEntity player = sender.getPlayer();

        final ChatMessage message = new ChatMessage(rawMessage, KiloEssentials.hasPermissionNode(player.getCommandSource(), EssentialPermission.CHAT_COLOR));

        try {
            if (ServerChat.pingEnabled && KiloEssentials.hasPermissionNode(player.getCommandSource(), EssentialPermission.CHAT_PING_OTHER)) {
                if (KiloEssentials.hasPermissionNode(player.getCommandSource(), EssentialPermission.CHAT_PING_EVERYONE)
                        && rawMessage.contains(ServerChat.everyone_template)) {
                    message.setMessage(message.getFormattedMessage().replaceAll(ServerChat.everyone_template, ServerChat.everyone_displayFormat), true);

                    for (final UUID subscriber : channel.getSubscribers()) {
                        ServerChat.pingPlayer(KiloServer.getServer().getPlayer(subscriber));
                    }
                }

                for (final String targetName : KiloServer.getServer().getPlayerManager().getPlayerNames()) {
                    final String format = ServerChat.senderFormat.replace("%PLAYER_NAME%", targetName);
                    final OnlineUser target = KiloServer.getServer().getOnlineUser(targetName);

                    if (message.getFormattedMessage().contains(format)) {
                        final boolean canPing = channel.isSubscribed(target) &&
                                KiloEssentials.hasPermissionNode(target.getCommandSource(), EssentialPermission.CHAT_GET_PINGED);

                        final String formatOfThisPing = canPing ? ServerChat.displayFormat : ServerChat.not_pinged_displayFormat;
                        message.setMessage(message.getFormattedMessage().replaceAll(format,
                                formatOfThisPing.replaceAll(
                                        "%PLAYER_DISPLAYNAME%", target.getDisplayName()) + "&r"), true);
                        if (ServerChat.pingSoundEnabled && canPing) ServerChat.pingPlayer(target.getPlayer());
                    }
                }
            }
        } catch (final Exception e) {
            KiloChat.sendMessageTo(sender.getPlayer(), new LiteralText(e.getMessage()));
        }

        message.setMessage(ConfigVariableFactory.replaceUserVariables(template, sender)
                .replace("%USER_RANKED_DISPLAYNAME%", sender.getRankedDisplayName().asFormattedString())
                .replace("%MESSAGE%", message.getFormattedMessage()), true);

        final Text text = new LiteralText(message.getFormattedMessage()).styled(style -> {
            style.setColor(Formatting.RESET);
            style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ServerChat.getHoverMessage(sender)));
            style.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + sender.getUsername() + ' '));
        });

        KiloServer.getServer().getUserManager().getOnlineUsers().forEach((uuid, user) -> {
            if (channel.isSubscribed(user))
                KiloChat.sendMessageTo(user.getPlayer(), text);
        });

        KiloServer.getServer().sendMessage(String.format("[Chat/%s] %s: %s", channel.getId(), sender.getUsername(), rawMessage));
    }

    private static Text getHoverMessage(final OnlineUser user) {
        final Text text = new LiteralText("");
        text.append(new LiteralText("[").formatted(Formatting.DARK_GRAY))
                .append(new LiteralText(" i ").formatted(Formatting.GREEN))
        .append(new LiteralText("] ").formatted(Formatting.DARK_GRAY));
        text.append(new LiteralText("Click here to reply to " + user.getFormattedDisplayName()).formatted(Formatting.GREEN));
        text.append("\n");
        text.append(new LiteralText("Sent at: ").formatted(Formatting.GRAY));
        text.append(new LiteralText(new Date().toGMTString()).formatted(Formatting.YELLOW));
        return text;
    }

    private static void pingPlayer(final ServerPlayerEntity target) {
        final Vec3d vec3d = target.getCommandSource().getPosition();
        final String soundId = "minecraft:" + ServerChat.config.ping().pingSound().id;
        final float volume = (float) ServerChat.config.ping().pingSound().volume;
        final float pitch = (float) ServerChat.config.ping().pingSound().pitch;

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

    public static int executeSend(final ServerCommandSource source, final ServerPlayerEntity target, final String message) throws CommandSyntaxException {
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
                new ChatMessage(toSource, true).getFormattedMessage()).formatted(Formatting.WHITE));
        KiloChat.sendMessageTo(target.getPlayer(), new LiteralText(
                new ChatMessage(toTarget, true).getFormattedMessage()).formatted(Formatting.WHITE));

        for (final OnlineServerUser user : KiloServer.getServer().getUserManager().getOnlineUsers().values()) {
            if (user.getSetting(Settings.SOCIAL_SPY) && !CommandUtils.areTheSame(source, user) && !CommandUtils.areTheSame(target, user))
                KiloChat.sendMessageTo(user.getPlayer(), new LiteralText(
                    new ChatMessage(toSpy, true).getFormattedMessage()).formatted(Formatting.WHITE));
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
                        new ChatMessage(toSpy, true).getFormattedMessage()).formatted(Formatting.GRAY));
            }
        }
    }

    private static final SimpleCommandExceptionType SAME_TARGETS_EXCEPTION = new SimpleCommandExceptionType(new LiteralText("You can't message your self!"));
    private static final SimpleCommandExceptionType TARGET_OFFLINE_EXCEPTION = new SimpleCommandExceptionType(new LiteralText("The Target player is offline!"));
    private static final SimpleCommandExceptionType CANT_MESSAGE_EXCEPTION = new  SimpleCommandExceptionType(LangText.getFormatter(true, "command.message.error"));
}
