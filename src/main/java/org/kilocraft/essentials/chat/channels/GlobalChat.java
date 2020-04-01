package org.kilocraft.essentials.chat.channels;

import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.chat.ServerChat;
import org.kilocraft.essentials.user.ServerUser;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GlobalChat implements ChatChannel {
    private List<UUID> subscribers;

    public GlobalChat() {
        this.subscribers = new ArrayList<>();
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
        return true;
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
        this.subscribers.add(user.getUuid());
    }

    @Override
    public void leave(ServerUser user) {
        this.subscribers.remove(user.getUuid());
    }

    public static String getChannelId() {
        return "global";
    }

}
