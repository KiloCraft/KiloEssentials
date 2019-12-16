package org.kilocraft.essentials.api.user;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.kilocraft.essentials.chat.ChatMessage;

public interface OnlineUser extends User {
    ServerPlayerEntity getPlayer();

    ServerCommandSource getCommandSource();

    void sendMessage(String message);

    void sendMessage(Text text);

    void sendMessage(ChatMessage chatMessage);

    void sendLangMessage(String key, Object... objects);

    void sendConfigMessage(String key, Object... objects);
}
