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
import org.kilocraft.essentials.EssentialPermissions;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.commands.CommandHelper;
import org.kilocraft.essentials.config.ConfigValueGetter;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.config.provided.localVariables.UserConfigVariables;
import org.kilocraft.essentials.user.ServerUser;

import java.util.Date;

import static org.kilocraft.essentials.api.KiloServer.getServer;

public class ServerChat {
    private static ConfigValueGetter config = KiloConfig.getProvider().getMain();
    private static ConfigValueGetter messages = KiloConfig.getProvider().getMessages();
    private static String template = config.getStringSafely("chat.messageFormat", "<%USER_DISPLAYNAME%> %MESSAGE%");
    private static String everyone_template = config.getStringSafely("chat.ping.format_everyone", "@everyone");
    private static String senderFormat = config.get(false, "chat.ping.format");
    private static String displayFormat = config.get(false, "chat.ping.pinged");
    private static String everyone_displayFormat = config.getStringSafely("chat.ping.pinged_everyone", "&b&o@everyone");

    public static void sendChatMessage(ServerPlayerEntity player, String messageToSend) {
        ServerUser user = (ServerUser) KiloServer.getServer().getUserManager().getOnline(player);
        boolean isPingingAllowed = config.getBooleanSafely("chat.ping.sound.enable", true);

        ChatMessage message = new ChatMessage(messageToSend,
                KiloEssentials.hasPermissionNode(player.getCommandSource(), EssentialPermissions.CHAT_COLOR.getNode()));

        if (config.getBooleanSafely("chat.ping.enable", true)
                && KiloEssentials.hasPermissionNode(player.getCommandSource(), EssentialPermissions.CHAT_PING_OTHER.getNode(), 3))

            //Ping Everyone
            if (KiloEssentials.hasPermissionNode(player.getCommandSource(), EssentialPermissions.CHAT_PING_EVERYONE.getNode(), 3)
                    && messageToSend.contains(everyone_template)) {
                message.setMessage(messageToSend.replace(everyone_template, everyone_displayFormat + "&r"), true);

                for (ServerPlayerEntity playerEntity : getServer().getPlayerManager().getPlayerList()) {
                    pingPlayer(playerEntity);
                }
            }

            //Ping singleton target
            for (String targetName : getServer().getPlayerManager().getPlayerNames()) {
                if (messageToSend.contains(senderFormat.replace("%PLAYER_NAME%", targetName))) {
                    OnlineUser target = KiloServer.getServer().getUserManager().getOnline(targetName);
                    String senderPing = senderFormat.replace("%PLAYER_NAME%", targetName);
                    String displayPing = "&r" + displayFormat.replace("%PLAYER_DISPLAYNAME%", target.getDisplayname()) + "&r";

                    boolean canPing = KiloEssentials.hasPermissionNode(
                            target.getCommandSource(), EssentialPermissions.CHAT_GET_PINGED.getNode(), 4);

                    if (isPingingAllowed && canPing) {
                        message.setMessage(messageToSend.replace(senderPing, displayPing), true);
                        pingPlayer(getServer().getPlayer(targetName));
                    }

                }
            }

        message.setMessage(config.getLocalReplacer()
                .replace(template, new UserConfigVariables(user), KiloConfig.getFileConfigOfMain())
                .replace("%USER_DISPLAYNAME%", user.getRankedDisplayname().asFormattedString())
                .replace("%MESSAGE%", message.getOriginal()), true);

        Text text = new LiteralText(message.getFormattedMessage()
        ).styled((style) -> {
            style.setColor(Formatting.RESET);
            style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, getHoverMessage(player)));
        });

        KiloChat.broadCast(text);
    }

    private static Text getHoverMessage(ServerPlayerEntity player) {
        Text text = new LiteralText("");
        text.append(new LiteralText("Sent at: ").formatted(Formatting.YELLOW));
        text.append(new LiteralText(new Date().toGMTString()).formatted(Formatting.GOLD));

        return text;
    }

    public static void pingPlayer(ServerPlayerEntity target) {
        Vec3d vec3d = target.getCommandSource().getPosition();
        String soundId = "minecraft:" + config.getValue("chat.ping.sound.id");
        float volume = config.getFloatSafely("chat.ping.sound.volume", "1.0");
        float pitch = config.getFloatSafely("chat.ping.sound.pitch", "1.0");

        target.networkHandler.sendPacket(new PlaySoundIdS2CPacket(new Identifier(soundId), SoundCategory.MASTER, vec3d, volume, pitch));
    }

    public static void sendPrivateMessage(ServerCommandSource source, OnlineUser target, String message) throws CommandSyntaxException {
        String format = config.getValue("chat.privateMessageFormat");
        String sourceName = CommandHelper.isConsole(source) ? source.getName() :
                KiloServer.getServer().getOnlineUser(source.getPlayer()).getRankedDisplayname().asFormattedString();

        String toSource = format.replace("%SOURCE%", "&r&cme&r")
                .replace("%TARGET%", "&r" + target.getRankedDisplayname().asFormattedString() + "&r").replace("%MESSAGE%", message);
        String toTarget = format.replace("%SOURCE%", sourceName).replace("%TARGET%", "&r&cme")
                .replace("%MESSAGE%", message);
        ;

        KiloChat.sendMessageToSource(source, new LiteralText(
                new ChatMessage(toSource, true).getFormattedMessage()).formatted(Formatting.GRAY));
        KiloChat.sendMessageTo(target.getPlayer(), new LiteralText(
                new ChatMessage(toTarget, true).getFormattedMessage()).formatted(Formatting.GRAY));
    }
}
