package org.kilocraft.essentials.craft.chat;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.network.MessageType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.chat.TextFormat;

public class KiloChat {
    public static void sendMessageTo(ServerPlayerEntity player, ChatMessage chatMessage) {
        sendMessageTo(player, chatMessage.getFormattedMessage());
    }

    public static void sendMessageTo(ServerCommandSource source, ChatMessage chatMessage) throws CommandSyntaxException {
        sendMessageTo(source.getPlayer(), chatMessage.getFormattedMessage());
    }

    public static void sendMessageTo(ServerPlayerEntity player, Text text) {
        player.sendChatMessage(text, MessageType.CHAT);
    }

    public static void broadCast(ServerPlayerEntity player, ChatMessage chatMessage) {
        KiloServer.getServer().getPlayerManager().getPlayerList().forEach((playerEntity) -> {
            playerEntity.sendChatMessage(chatMessage.getFormattedMessage(), MessageType.CHAT);
        });

        KiloServer.getServer().sendMessage(TextFormat.removeAlternateColorCodes('&', chatMessage.getFormattedAsString()));
    }
}
