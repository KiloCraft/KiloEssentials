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
import org.kilocraft.essentials.craft.config.provided.localVariables.PlayerConfigVariables;

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

    public static void broadCast(ChatMessage chatMessage) {
        KiloServer.getServer().getPlayerManager().getPlayerList().forEach((playerEntity) -> {
            playerEntity.sendChatMessage(chatMessage.getFormattedMessage(), MessageType.CHAT);
        });

        KiloServer.getServer().sendMessage(TextFormat.removeAlternateColorCodes('&', chatMessage.getFormattedAsString()));
    }

    public static void sendChatMessage(ServerPlayerEntity player, ChatMessageC2SPacket packet) {
        ChatMessage message = new ChatMessage(
                packet.getChatMessage(),
                Thimble.hasPermissionChildOrOp(player.getCommandSource(), "kiloessentials.chat.format", 3)
        );

        broadCast(
                new ChatMessage(
                        KiloConifg.getProvider().getMessages().getLocal(
                                true,
                                "general.messageFormat",
                                new PlayerConfigVariables(player)
                        ).replace("%MESSAGE%", message.getFormattedAsString())
                        .replace("%PLAYER_DISPLAYNAME%", player.getDisplayName().asFormattedString()),
                        true
                )
        );
    }


}