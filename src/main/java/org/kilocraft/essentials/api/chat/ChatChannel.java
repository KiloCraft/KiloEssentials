package org.kilocraft.essentials.api.chat;

import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.user.ServerUser;

import java.util.Map;
import java.util.UUID;

public interface ChatChannel {

    String getId();

    void onChatMessage(ServerPlayerEntity player, String message);

    void sendChatMessage(OnlineUser user, String messageToSend);

    boolean isPublic();

    boolean isSubscribed(OnlineUser user);

    Map<UUID, String> getSubscribers();

    void join(ServerUser user);

    void leave(ServerUser user);

}
