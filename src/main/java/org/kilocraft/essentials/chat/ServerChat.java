package org.kilocraft.essentials.chat;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.network.packet.PlaySoundIdS2CPacket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
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
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.commands.CommandHelper;
import org.kilocraft.essentials.config.ConfigValueGetter;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.config.provided.localVariables.UserConfigVariables;
import org.kilocraft.essentials.user.OnlineServerUser;
import org.kilocraft.essentials.user.ServerUser;

import java.util.Date;
import java.util.UUID;

import static org.kilocraft.essentials.api.KiloServer.getServer;

public class ServerChat {
    private static ConfigValueGetter config = KiloConfig.getProvider().getMain();
    private static ConfigValueGetter messages = KiloConfig.getProvider().getMessages();
    private static String everyone_template = config.getStringSafely("chat.ping.format_everyone", "@everyone");
    private static String senderFormat = config.get(false, "chat.ping.format");
    private static String displayFormat = config.get(false, "chat.ping.pinged");
    private static String everyone_displayFormat = config.getStringSafely("chat.ping.pinged_everyone", "&b&o@everyone");

    public static void send(OnlineUser sender, String rawMessage, ChatChannel channel) {
        String template = config.getStringSafely("chat.channels.formats." + channel.getId(), "&r[&r%USER_DISPLAYNAME%&r]:&r %MESSAGE%");
        ServerPlayerEntity player = sender.getPlayer();
        boolean isPingingAllowed = config.getBooleanSafely("chat.ping.sound.enable", true);

        ChatMessage message = new ChatMessage(rawMessage,
                KiloEssentials.hasPermissionNode(player.getCommandSource(), EssentialPermission.CHAT_COLOR));

        if (config.getBooleanSafely("chat.ping.enable", true)
                && KiloEssentials.hasPermissionNode(player.getCommandSource(), EssentialPermission.CHAT_PING_OTHER)) {

            //Ping Everyone
            if (KiloEssentials.hasPermissionNode(player.getCommandSource(), EssentialPermission.CHAT_PING_EVERYONE)
                    && rawMessage.contains(everyone_template)) {
                message.setMessage(rawMessage.replace(everyone_template, everyone_displayFormat + "&r"), true);

                for (UUID subscriber : channel.getSubscribers()) {
                    pingPlayer(KiloServer.getServer().getPlayer(subscriber));
                }
            }

            //Ping singleton target
            for (String targetName : getServer().getPlayerManager().getPlayerNames()) {
                if (rawMessage.contains(senderFormat.replace("%PLAYER_NAME%", targetName))) {
                    OnlineUser target = KiloServer.getServer().getUserManager().getOnline(targetName);

                    if (channel.isSubscribed(target)) {
                        assert target != null;
                        boolean canPing = KiloEssentials.hasPermissionNode(target.getCommandSource(), EssentialPermission.CHAT_GET_PINGED);

                        if (isPingingAllowed && canPing) {
                            String senderPing = senderFormat.replace("%PLAYER_NAME%", targetName);
                            String displayPing = "&r" + displayFormat.replace("%PLAYER_DISPLAYNAME%",
                                    target.getRankedDisplayname().asFormattedString()) + "&r";

                            message.setMessage(rawMessage.replace(senderPing, displayPing), true);
                            pingPlayer(getServer().getPlayer(targetName));
                        }
                    }
                }
            }
        }

        message.setMessage(config.getLocalReplacer()
                .replace(template, new UserConfigVariables((ServerUser) sender), KiloConfig.getFileConfigOfMain())
                .replace("%USER_DISPLAYNAME%", sender.getRankedDisplayname().asFormattedString())
                .replace("%MESSAGE%", message.getFormattedMessage()), true);

        Text text = new LiteralText(message.getFormattedMessage()
        ).styled((style) -> {
            style.setColor(Formatting.RESET);
            style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, getHoverMessage(player)));
        });

        player.sendMessage(text);
        KiloChat.broadCastToConsole(new ChatMessage(text.asString(), false));

        for (UUID subscriber : channel.getSubscribers()) {
            if (!subscriber.equals(player.getUuid()))
                KiloChat.sendMessageTo(KiloServer.getServer().getPlayer(subscriber), text);
        }
    }

    private static Text getHoverMessage(ServerPlayerEntity player) {
        Text text = new LiteralText("");
        text.append(new LiteralText("Sent at: ").formatted(Formatting.YELLOW));
        text.append(new LiteralText(new Date().toGMTString()).formatted(Formatting.GOLD));

        return text;
    }

    private static void pingPlayer(ServerPlayerEntity target) {
        Vec3d vec3d = target.getCommandSource().getPosition();
        String soundId = "minecraft:" + config.getValue("chat.ping.sound.id");
        float volume = config.getFloatSafely("chat.ping.sound.volume", "1.0");
        float pitch = config.getFloatSafely("chat.ping.sound.pitch", "1.0");

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

    public static void sendPrivateMessage(ServerCommandSource source, OnlineUser target, String message) throws CommandSyntaxException {
        String format = config.getStringSafely("chat.privateMessages.format", "&r%USER_DISPLAYNAME% &8>>&r %MESSAGE%") + "&r";
        String me_format = config.getStringSafely("chat.privateMessages.me_format", "&cme") + "&r";
        String sourceName = CommandHelper.isConsole(source) ? source.getName() :
                KiloServer.getServer().getOnlineUser(source.getPlayer()).getRankedDisplayname().asFormattedString();

        String toSource = format.replace("%SOURCE%", me_format)
                .replace("%TARGET%", "&r" + target.getRankedDisplayname().asFormattedString() + "&r")
                .replace("%MESSAGE%", message);
        String toTarget = format.replace("%SOURCE%", sourceName)
                .replace("%TARGET%", me_format)
                .replace("%MESSAGE%", message);

        String toSpy = format.replace("%SOURCE%", sourceName)
                .replace("%TARGET%", target.getUsername() + "&r")
                .replace("%MESSAGE%", message);

        KiloChat.sendMessageToSource(source, new LiteralText(
                new ChatMessage(toSource, true).getFormattedMessage()).formatted(Formatting.GRAY));
        KiloChat.sendMessageTo(target.getPlayer(), new LiteralText(
                new ChatMessage(toTarget, true).getFormattedMessage()).formatted(Formatting.GRAY));

        for (OnlineServerUser user : KiloServer.getServer().getUserManager().getOnlineUsers().values()) {
            if (user.isSocialSpyOn() && !CommandHelper.areTheSame(target, user)) KiloChat.sendMessageTo(user.getPlayer(), new LiteralText(
                    new ChatMessage(toSpy, true).getFormattedMessage()).formatted(Formatting.GRAY));
        }

    }

    public static void sendCommandSpy(ServerCommandSource source, String message) {
        String format = config.getStringSafely("commandSpy.format", "&r&7%SOURCE% &3->&r /%MESSAGE%") + "&r";
        String toSpy = format.replace("%SOURCE%", source.getName())
                .replace("%MESSAGE%",  message);
        for (OnlineServerUser user : KiloServer.getServer().getUserManager().getOnlineUsers().values()) {
            if (user.isCommandSpyOn() && !CommandHelper.areTheSame(source, user)) KiloChat.sendMessageTo(user.getPlayer(), new LiteralText(
                    new ChatMessage(toSpy, true).getFormattedMessage()).formatted(Formatting.GRAY));
        }
    }
}
