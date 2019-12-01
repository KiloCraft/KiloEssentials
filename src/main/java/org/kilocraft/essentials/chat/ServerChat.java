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
        String senderFormat = config.get(false, "chat.ping.format");
        String displayFormat = config.get(false, "chat.ping.pinged");
        ServerUser user = (ServerUser) KiloServer.getServer().getUserManager().getOnline(player);

        ChatMessage message = new ChatMessage(messageToSend,
                KiloEssentials.hasPermissionNode(player.getCommandSource(), "chat.color")
        );

        if (config.getBooleanSafely("chat.ping.enable", true)
                && KiloEssentials.hasPermissionNode(player.getCommandSource(), "chat.ping"))
            for (String targetName : getServer().getPlayerManager().getPlayerNames()) {
                if (messageToSend.contains(senderFormat.replace("%PLAYER_NAME%", targetName))) {
                    OnlineUser target = KiloServer.getServer().getUserManager().getOnline(targetName);
                    String senderPing = senderFormat.replace("%PLAYER_NAME%", targetName);
                    String displayPing = "&r" + displayFormat.replace("%PLAYER_DISPLAYNAME%", target.getDisplayname()) + "&r";

                    message.setMessage(messageToSend.replace(senderPing, displayPing), true);

                    if ((boolean) config.getValue("chat.ping.sound.enable") &&
                            KiloEssentials.hasPermissionNode(player.getCommandSource(), "chat.ping.other"))
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
        text.append(new LiteralText(new Date().toString()).formatted(Formatting.GOLD));

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
