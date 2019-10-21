package org.kilocraft.essentials.craft.chat;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.network.MessageType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.packet.ChatMessageC2SPacket;
import net.minecraft.text.Text;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.chat.TextFormat;
import org.kilocraft.essentials.craft.config.KiloConifg;
import org.kilocraft.essentials.craft.config.provided.ConfigValueGetter;
import org.kilocraft.essentials.craft.config.provided.localVariables.PlayerConfigVariables;

public class KiloChat {
    private static ConfigValueGetter config = KiloConifg.getProvider().getMain();

    private static boolean enablePing = config.getValue("chat.ping.enable");
    private static String pingSenderFormat = config.get(false, "chat.ping.format");
    private static String pingFormat = config.get(false, "chat.ping.format");


    public static void sendMessageTo(ServerPlayerEntity player, ChatMessage chatMessage) {
        sendMessageTo(player, chatMessage.getFormattedMessage());
    }

    public static void sendMessageTo(ServerCommandSource source, ChatMessage chatMessage) throws CommandSyntaxException {
        sendMessageTo(source.getPlayer(), chatMessage.getFormattedMessage());
    }

    public static void sendMessageTo(ServerPlayerEntity player, Text text) {
        player.sendChatMessage(text, MessageType.CHAT);
    }

    public static void broadCast(ChatMessage chatMessage) {
        KiloServer.getServer().getPlayerManager().getPlayerList().forEach((playerEntity) -> {
            playerEntity.sendChatMessage(chatMessage.getFormattedMessage(), MessageType.CHAT);
        });

        KiloServer.getServer().sendMessage(TextFormat.removeAlternateColorCodes('&', chatMessage.getFormattedAsString()));
    }

    public static void sendChatMessage(ServerPlayerEntity player, ChatMessageC2SPacket packet) {
        ChatMessage message = new ChatMessage(
                packet.getChatMessage(),
                Thimble.hasPermissionOrOp(player.getCommandSource(), "kiloessentials.chat.format", 3)
        );

        for (String playerName : KiloServer.getServer().getPlayerManager().getPlayerNames()) {
            if (packet.getChatMessage().contains(playerName)) {

            }
        }



        broadCast(
                new ChatMessage(
                        config.getLocal(
                                true,
                                "chat.messageFormat",
                                new PlayerConfigVariables(player)
                        ).replace("%MESSAGE%", message.getFormattedAsString())
                        .replace("%PLAYER_DISPLAYNAME%", player.getDisplayName().asFormattedString()),
                        true
                )
        );
    }

    public static void sendChatMessagePingPlayer(ServerPlayerEntity player, ChatMessage chatMessage, String playerToPing) {

    }


}