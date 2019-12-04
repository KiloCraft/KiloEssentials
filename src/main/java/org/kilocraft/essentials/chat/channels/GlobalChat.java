package org.kilocraft.essentials.chat.channels;

import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.chat.ChatChannel;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.chat.ServerChat;
import org.kilocraft.essentials.user.ServerUser;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GlobalChat implements ChatChannel {
    private Map<UUID, String> members;

    public GlobalChat() {
        this.members = new HashMap<>();
    }

    @Override
    public String getId() {
        return "global";
    }

    @Override
    public void onChatMessage(ServerPlayerEntity player, String message) {
        OnlineUser user = KiloServer.getServer().getOnlineUser(player);

        if (isSubscribed(user))
            sendChatMessage(user, message);
    }

    @Override
    public void sendChatMessage(OnlineUser user, String messageToSend) {
        ServerChat.sendChatMessage(user.getPlayer(), messageToSend);
    }

    @Override
    public boolean isPublic() {
        return true;
    }

    @Override
    public boolean isSubscribed(OnlineUser user) {
        return this.members.containsKey(user.getUuid());
    }

    @Override
    public Map<UUID, String> getSubscribers() {
        return null;
    }

    @Override
    public void join(ServerUser user) {
        this.members.put(user.getUuid(), user.getUsername());
    }

    @Override
    public void leave(ServerUser user) {
        this.members.remove(user.getUuid());
    }

}
