package org.kilocraft.essentials.chat;

import net.minecraft.client.network.packet.PlaySoundIdS2CPacket;
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
import org.kilocraft.essentials.config.ConfigValueGetter;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.config.provided.localVariables.UserConfigVariables;
import org.kilocraft.essentials.user.ServerUser;

import java.util.Date;

import static org.kilocraft.essentials.api.KiloServer.getServer;

public class ServerChat {
    private static ConfigValueGetter config = KiloConfig.getProvider().getMain();
    private static ConfigValueGetter messages = KiloConfig.getProvider().getMessages();

    public static void sendChatMessage(ServerPlayerEntity player, String messageToSend) {
        String template = config.getStringSafely("chat.messageFormat", "<%USER_DISPLAYNAME%> %MESSAGE%");
        String everyone_template = config.getStringSafely("chat.ping.format_everyone", "@everyone");
        String senderFormat = config.get(false, "chat.ping.format");
        String displayFormat = config.get(false, "chat.ping.pinged");
        String everyone_displayFormat = config.getStringSafely("chat.ping.pinged_everyone", "&b&o@everyone");
        ServerUser user = (ServerUser) KiloServer.getServer().getUserManager().getOnline(player);

        ChatMessage message = new ChatMessage(messageToSend,
                KiloEssentials.hasPermissionNode(player.getCommandSource(), EssentialPermissions.CHAT_COLOR.getNode())
        );

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

                    message.setMessage(messageToSend.replace(senderPing, displayPing), true);

                    if (config.getBooleanSafely("chat.ping.sound.enable", true) &&
                            KiloEssentials.hasPermissionNode(
                                    KiloServer.getServer().getPlayer(target.getUuid()).getCommandSource(),
                                    EssentialPermissions.CHAT_GET_PINGED.getNode(), 4)
                    )
                        pingPlayer(getServer().getPlayer(targetName));
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
}
