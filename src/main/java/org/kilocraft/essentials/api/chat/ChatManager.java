package org.kilocraft.essentials.api.chat;

import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.chat.ServerChat;
import org.kilocraft.essentials.chat.channels.GlobalChat;
import org.kilocraft.essentials.user.setting.Settings;
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

        if (channels.containsKey(user.getSetting(Settings.CHAT_CHANNEL).getId()))
            channels.get(user.getSetting(Settings.CHAT_CHANNEL).getId()).onChatMessage(player, packet.getChatMessage());
        else {
            user.getSettings().reset(Settings.CHAT_CHANNEL);
            String errorMessage = String.format(
                    KiloEssentials.getInstance().getMessageUtil().fromExceptionNode(ExceptionMessageNode.INVALID_CHAT_UPSTREAM_ID),
                    user.getUuid().toString(), user.getSetting(Settings.CHAT_CHANNEL));

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
