package org.kilocraft.essentials.api.chat;

import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.chat.channels.GlobalChat;
import org.kilocraft.essentials.util.messages.nodes.ExceptionMessageNode;

import java.util.HashMap;
import java.util.Map;

public class ChatManager<C extends ChatChannel> {
    public Map<String, ChatChannel> channels;

    public ChatManager() {
        this.channels = new HashMap<>();
    }

    public <C extends ChatChannel> void register(C c) {
        this.channels.put(c.getId(), c);
    }

    public void onChatMessage(ServerPlayerEntity player, ChatMessageC2SPacket packet) {
        OnlineUser user = KiloServer.getServer().getOnlineUser(player);

        if (channels.containsKey(user.getUpstreamChannelId()))
            channels.get(user.getUpstreamChannelId()).onChatMessage(player, packet.getChatMessage());
        else {
            user.setUpstreamChannelId(GlobalChat.getChannelId());
            String errorMessage = String.format(
                    KiloEssentials.getInstance().getMessageUtil().fromExceptionNode(ExceptionMessageNode.INVALID_CHAT_UPSTREAM_ID),
                    user.getUuid().toString(), user.getUpstreamChannelId());

            user.getCommandSource().sendError(new LiteralText(errorMessage));
            KiloEssentials.getLogger().error(errorMessage);
        }
    }

    public Map<String, ChatChannel> getChannels() {
        return this.channels;
    }

    public ChatChannel getChannel(String id) {
        return channels.get(id);
    }

}
