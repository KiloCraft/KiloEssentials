package org.kilocraft.essentials.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.chat.ServerChat;

import java.util.List;

public class ChatEvents {

    public static final Event<ChatEvent> CHAT_MESSAGE = EventFactory.createArrayBacked(ChatEvent.class, (callbacks) -> (player, message, channel) -> {
        for (ChatEvent callback : callbacks) {
            callback.onChat(player, message, channel);
        }
    });

    public interface ChatEvent {
        void onChat(ServerPlayer player, String message, ServerChat.Channel channel);
    }

    public static final Event<DirectMessageEvent> DIRECT_MESSAGE = EventFactory.createArrayBacked(DirectMessageEvent.class, (callbacks) -> (source, receiver, message) -> {
        for (DirectMessageEvent callback : callbacks) {
            callback.onDirectMessage(source, receiver, message);
        }
    });

    public interface DirectMessageEvent {
        void onDirectMessage(CommandSourceStack source, OnlineUser receiver, String message);
    }

    public static final Event<FlaggedMessageEvent> FLAGGED_MESSAGE = EventFactory.createArrayBacked(FlaggedMessageEvent.class, (callbacks) -> (source, receiver, message) -> {
        for (FlaggedMessageEvent callback : callbacks) {
            callback.onMessageFlag(source, receiver, message);
        }
    });

    public interface FlaggedMessageEvent {
        void onMessageFlag(OnlineUser sender, final String input, final List<String> flagged);
    }

}
