package org.kilocraft.essentials.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.chat.ServerChat;

public class ChatEvents {

    public static final Event<ChatEvent> CHAT_MESSAGE = EventFactory.createArrayBacked(ChatEvent.class, (callbacks) -> (player, message, channel) -> {
        for (ChatEvent callback : callbacks) {
            callback.onChat(player, message, channel);
        }
    });

    public interface ChatEvent {
        void onChat(ServerPlayerEntity player, String message, ServerChat.Channel channel);
    }

    public static final Event<DirectMessageEvent> DIRECT_MESSAGE = EventFactory.createArrayBacked(DirectMessageEvent.class, (callbacks) -> (source, receiver, message) -> {
        for (DirectMessageEvent callback : callbacks) {
            callback.onDirectMessage(source, receiver, message);
        }
    });

    public interface DirectMessageEvent {
        void onDirectMessage(ServerCommandSource source, OnlineUser receiver, String message);
    }
    
}
