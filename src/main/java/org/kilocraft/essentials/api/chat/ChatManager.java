package org.kilocraft.essentials.api.chat;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.packet.ChatMessageC2SPacket;

import java.util.HashMap;
import java.util.Map;

public class ChatManager<C extends ChatChannel> {
    private Map<String, ChatChannel> channels;

    public ChatManager() {
        this.channels = new HashMap<>();
    }

    public <C extends ChatChannel> void register(C c) {
        this.channels.put(c.getId(), c);
    }

    public void onChatMessage(ServerPlayerEntity player, ChatMessageC2SPacket packet) {
        channels.forEach((id, channel) -> channel.onChatMessage(player, packet.getChatMessage()));
    }

    public Map<String, ChatChannel> getChannels() {
        return this.channels;
    }

    public ChatChannel getChannel(String id) {
        return channels.get(id);
    }

}
