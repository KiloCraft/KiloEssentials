package org.kilocraft.essentials.craft.chat;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.network.MessageType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public class KiloChat {
    public static void sendMessageTo(ServerPlayerEntity player, ChatMessage chatMessage) {
        sendMessageTo(player, new LiteralText(chatMessage.getMessage()));
    }

    public static void sendMessageTo(ServerCommandSource source, ChatMessage chatMessage) throws CommandSyntaxException {
        sendMessageTo(source.getPlayer(), new LiteralText(chatMessage.getMessage()));
    }

    public static void sendMessageTo(ServerPlayerEntity player, Text text) {
        player.sendChatMessage(text, MessageType.CHAT);
    }
}
