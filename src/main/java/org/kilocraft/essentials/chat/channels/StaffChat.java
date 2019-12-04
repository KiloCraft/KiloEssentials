package org.kilocraft.essentials.chat.channels;

import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.api.chat.ChatChannel;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.user.ServerUser;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StaffChat implements ChatChannel {
    private Map<UUID, String> members;

    public StaffChat() {
        this.members = new HashMap<>();
    }

    @Override
    public String getId() {
        return "staff";
    }

    @Override
    public void onChatMessage(ServerPlayerEntity player, String message) {

    }

    @Override
    public void sendChatMessage(OnlineUser user, String messageToSend) {

    }

    @Override
    public boolean isPublic() {
        return false;
    }

    @Override
    public boolean isSubscribed(OnlineUser user) {
        return this.members.containsKey(user.getUuid());
    }

    @Override
    public Map<UUID, String> getSubscribers() {
        return this.members;
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
