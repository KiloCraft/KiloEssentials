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
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.commands.CommandHelper;
import org.kilocraft.essentials.config.ConfigVariableFactory;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.config.main.sections.chat.ChatConfigSection;
import org.kilocraft.essentials.user.OnlineServerUser;
import org.kilocraft.essentials.user.ServerUser;

import java.util.Date;
import java.util.UUID;

import static org.kilocraft.essentials.api.KiloServer.getServer;

public class ServerChat {
    private static ChatConfigSection config = KiloConfig.main().chat();
    private static String everyone_template = config.ping().everyoneTypedFormat;
    private static String senderFormat = config.ping().typedFormat;
    private static String displayFormat = config.ping().pingedFormat;
    private static String not_pinged_displayFormat = config.ping().pingedNotPingedFormat;
    private static String everyone_displayFormat = config.ping().everyonePingedFormat;
    private static boolean pingSoundEnabled = config.ping().pingSound().enabled;
    private static boolean pingEnabled = config.ping().enabled;

    public static void send(OnlineUser sender, String rawMessage, ChatChannel channel) {
        String template = KiloConfig.getMainNode().getNode("chat").getNode("channelsMeta").getNode(channel.getId() + "Chat").getString();
        ServerPlayerEntity player = sender.getPlayer();

        ChatMessage message = new ChatMessage(rawMessage, KiloEssentials.hasPermissionNode(player.getCommandSource(), EssentialPermission.CHAT_COLOR));

        try {
            if (pingEnabled && KiloEssentials.hasPermissionNode(player.getCommandSource(), EssentialPermission.CHAT_PING_OTHER)) {
                if (KiloEssentials.hasPermissionNode(player.getCommandSource(), EssentialPermission.CHAT_PING_EVERYONE)
                        && rawMessage.contains(everyone_template)) {
                    message.setMessage(message.getFormattedMessage().replaceAll(everyone_template, everyone_displayFormat), true);

                    for (UUID subscriber : channel.getSubscribers()) {
                        pingPlayer(KiloServer.getServer().getPlayer(subscriber));
                    }
                }

                for (String targetName : getServer().getPlayerManager().getPlayerNames()) {
                    String format = senderFormat.replace("%PLAYER_NAME%", targetName);
                    OnlineUser target = KiloServer.getServer().getOnlineUser(targetName);

                    if (message.getFormattedMessage().contains(format)) {
                        boolean canPing = channel.isSubscribed(target) &&
                                KiloEssentials.hasPermissionNode(target.getCommandSource(), EssentialPermission.CHAT_GET_PINGED);

                        String formatOfThisPing = canPing ? displayFormat : not_pinged_displayFormat;
                        message.setMessage(message.getFormattedMessage().replaceAll(format,
                                formatOfThisPing.replaceAll(
                                        "%PLAYER_DISPLAYNAME%", target.getDisplayName()) + "&r"), true);
                        if (pingSoundEnabled && canPing) pingPlayer(target.getPlayer());
                    }
                }
            }
        } catch (Exception e) {
            KiloChat.sendMessageTo(sender.getPlayer(), new LiteralText(e.getMessage()));
        }

        message.setMessage(ConfigVariableFactory.replaceUserVariables(template, sender)
                .replace("%USER_RANKED_DISPLAYNAME%", sender.getRankedDisplayName().asFormattedString())
                .replace("%MESSAGE%", message.getFormattedMessage()), true);

        Text text = new LiteralText(message.getFormattedMessage()).styled((style) -> {
            style.setColor(Formatting.RESET);
            style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, getHoverMessage(sender)));
            style.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + sender.getUsername() + " "));
        });

        KiloServer.getServer().getUserManager().getOnlineUsers().forEach((uuid, user) -> {
            if (channel.isSubscribed(user))
                KiloChat.sendMessageTo(user.getPlayer(), text);
        });

        KiloServer.getServer().sendMessage(String.format("[Chat/%s] %s: %s", channel.getId(), sender.getUsername(), rawMessage));
    }

    private static Text getHoverMessage(OnlineUser user) {
        Text text = new LiteralText("");
        text.append(new LiteralText("[").formatted(Formatting.DARK_GRAY))
                .append(new LiteralText(" i ").formatted(Formatting.GREEN))
        .append(new LiteralText("] ").formatted(Formatting.DARK_GRAY));
        text.append(new LiteralText("Click here to reply to " + user.getFormattedDisplayName()).formatted(Formatting.GREEN));
        text.append("\n");
        text.append(new LiteralText("Sent at: ").formatted(Formatting.GRAY));
        text.append(new LiteralText(new Date().toGMTString()).formatted(Formatting.YELLOW));
        return text;
    }

    private static void pingPlayer(ServerPlayerEntity target) {
        Vec3d vec3d = target.getCommandSource().getPosition();
        String soundId = "minecraft:" + config.ping().pingSound().id;
        float volume = (float) config.ping().pingSound().volume;
        float pitch = (float) config.ping().pingSound().pitch;

        target.networkHandler.sendPacket(new PlaySoundIdS2CPacket(new Identifier(soundId), SoundCategory.MASTER, vec3d, volume, pitch));
    }

    public static void addSocialSpy(ServerPlayerEntity player) {
        KiloServer.getServer().getOnlineUser(player).setSocialSpyOn(true);
    }

    public static void removeSocialSpy(ServerPlayerEntity player) {
        KiloServer.getServer().getOnlineUser(player).setSocialSpyOn(false);
    }

    public static boolean isSocialSpy(ServerPlayerEntity player) {
        return KiloServer.getServer().getOnlineUser(player).isSocialSpyOn();
    }

    public static void addCommandSpy(ServerPlayerEntity player) {
        KiloServer.getServer().getOnlineUser(player).setCommandSpyOn(true);
    }

    public static void removeCommandSpy(ServerPlayerEntity player) {
        KiloServer.getServer().getOnlineUser(player).setCommandSpyOn(false);
    }

    public static boolean isCommandSpy(ServerPlayerEntity player) {
        return KiloServer.getServer().getOnlineUser(player).isCommandSpyOn();
    }

    public static int executeSend(ServerCommandSource source, ServerPlayerEntity target, String message) throws CommandSyntaxException {
        if  (!CommandHelper.isConsole(source)) {
            OnlineUser user = KiloServer.getServer().getOnlineUser(target);
            OnlineUser srcUser = KiloServer.getServer().getOnlineUser(source.getPlayer());
            user.setLastMessageSender(source.getPlayer().getUuid());
            srcUser.setLastMessageSender(target.getUuid());
            srcUser.setLastPrivateMessage(message);
        }

        if (target == null)
            throw TARGET_OFFLINE_EXCEPTION.create();

        if (CommandHelper.areTheSame(source, target))
            throw SAME_TARGETS_EXCEPTION.create();

        ServerChat.sendPrivateMessage(source, KiloServer.getServer().getOnlineUser(target), message);

        return 1;
    }

    public static void sendPrivateMessage(ServerCommandSource source, OnlineUser target, String message) throws CommandSyntaxException {
        String format = config.privateChat().privateChat;
        String me_format = config.privateChat().privateChatMeFormat;
        String sourceName = source.getName();

        if (CommandHelper.isPlayer(source) && ((ServerUser) target).getIgnoreList() != null &&
                ((ServerUser) target).getIgnoreList().containsValue(source.getPlayer().getUuid())) {
            throw new SimpleCommandExceptionType(LangText.getFormatter(true, "command.message.error")).create();
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

        KiloChat.sendMessageToSource(source, new LiteralText(
                new ChatMessage(toSource, true).getFormattedMessage()).formatted(Formatting.WHITE));
        KiloChat.sendMessageTo(target.getPlayer(), new LiteralText(
                new ChatMessage(toTarget, true).getFormattedMessage()).formatted(Formatting.WHITE));

        for (OnlineServerUser user : KiloServer.getServer().getUserManager().getOnlineUsers().values()) {
            if (user.isSocialSpyOn() && !CommandHelper.areTheSame(source, user) && !CommandHelper.areTheSame(target, user))
                KiloChat.sendMessageTo(user.getPlayer(), new LiteralText(
                    new ChatMessage(toSpy, true).getFormattedMessage()).formatted(Formatting.WHITE));
        }

        KiloServer.getServer().sendMessage(String.format("[Chat/Private] %s -> %s: %s", source.getName(), target.getUsername(), message));
    }

    public static void sendCommandSpy(ServerCommandSource source, String message) {
        String format = config.commandSpyFormat;
        String toSpy = format.replace("%SOURCE%", source.getName())
                .replace("%MESSAGE%",  message);

        for (OnlineServerUser user : KiloServer.getServer().getUserManager().getOnlineUsers().values()) {
            if (user.isCommandSpyOn() && !CommandHelper.areTheSame(source, user))
                KiloChat.sendMessageTo(user.getPlayer(), new LiteralText(
                        new ChatMessage(toSpy, true).getFormattedMessage()).formatted(Formatting.GRAY));
        }
    }

    private static final SimpleCommandExceptionType SAME_TARGETS_EXCEPTION = new SimpleCommandExceptionType(new LiteralText("You can't message your self!"));
    private static final SimpleCommandExceptionType TARGET_OFFLINE_EXCEPTION = new SimpleCommandExceptionType(new LiteralText("The Target player is offline!"));
}
