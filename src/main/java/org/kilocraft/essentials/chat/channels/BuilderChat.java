package org.kilocraft.essentials.chat.channels;

import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.chat.ChatChannel;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.chat.ChatMessage;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.chat.ServerChat;
import org.kilocraft.essentials.config_old.ConfigValueGetter;
import org.kilocraft.essentials.config_old.KiloConfig;
import org.kilocraft.essentials.user.ServerUser;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BuilderChat implements ChatChannel {
    private static ConfigValueGetter config = KiloConfig.getProvider().getMain();
    private List<UUID> subscribers;

    public BuilderChat() {
        this.subscribers = new ArrayList<>();
    }

    public static String getChannelId() {
        return "builder";
    }

    @Override
    public String getId() {
        return getChannelId();
    }

    @Override
    public void onChatMessage(ServerPlayerEntity player, String message) {
        OnlineUser user = KiloServer.getServer().getOnlineUser(player);
        sendChatMessage(user, message);
    }

    @Override
    public void sendChatMessage(OnlineUser user, String messageToSend) {
        ServerChat.send(user, messageToSend, this);
    }

    @Override
    public boolean isPublic() {
        return false;
    }

    @Override
    public boolean isSubscribed(OnlineUser user) {
        return this.subscribers.contains(user.getUuid());
    }

    @Override
    public List<UUID> getSubscribers() {
        return this.subscribers;
    }

    @Override
    public void join(ServerUser user) {
        if (isSubscribed((OnlineUser) user))
            return;

        this.subscribers.add(user.getUuid());
        sendToSubscribers(new ChatMessage(
                config.getFormatter(true, "chat.channels.meta.builder_prefix") +
                        config.getFormatter(true, "chat.channels.messages.join",
                                user.getUsername() + "&r", getChannelId()),
                true));
    }

    @Override
    public void leave(ServerUser user) {
        if (!isSubscribed((OnlineUser) user))
            return;

        sendToSubscribers(new ChatMessage(
                config.getFormatter(true, "chat.channels.meta.builder_prefix") +
                        config.getFormatter(true, "chat.channels.messages.leave",
                                user.getUsername() + "&r", getChannelId()),
                true));
        this.subscribers.remove(user.getUuid());
    }

    private void sendToSubscribers(ChatMessage chatMessage) {
        for (UUID subscriber : this.subscribers) {
            KiloChat.sendMessageTo(KiloServer.getServer().getPlayer(subscriber), chatMessage);
        }
    }
}
