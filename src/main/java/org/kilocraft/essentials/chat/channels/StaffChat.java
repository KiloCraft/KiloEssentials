package org.kilocraft.essentials.chat.channels;

import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.EssentialPermission;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.chat.ServerChat;
import org.kilocraft.essentials.user.ServerUser;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StaffChat implements ChatChannel {
    public StaffChat() {
    }

    public static String getChannelId() {
        return "staff";
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
        return KiloEssentials.hasPermissionNode(user.getCommandSource(), EssentialPermission.STAFF, 2);
    }

    @Override
    public List<UUID> getSubscribers() {
        List<UUID> uuids = new ArrayList<>();
        for (ServerPlayerEntity playerEntity : KiloServer.getServer().getPlayerManager().getPlayerList()) {
            if (KiloEssentials.hasPermissionNode(playerEntity.getCommandSource(), EssentialPermission.STAFF, 2))
                uuids.add(playerEntity.getUuid());
        }

        return uuids;
    }

    @Override
    public void join(ServerUser user) {
    }

    @Override
    public void leave(ServerUser user) {
    }

}
