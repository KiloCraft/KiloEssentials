package org.kilocraft.essentials.events.player;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.event.player.PlayerOnChatMessageEvent;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.chat.ServerChat;

public class PlayerOnChatMessageEventImpl implements PlayerOnChatMessageEvent {
    private final ServerPlayerEntity player;
    private final ServerChat.Channel channel;
    private String message;
    private boolean cancelled = false;
    private String cancelReason;

    public PlayerOnChatMessageEventImpl(@NotNull final ServerPlayerEntity player, @NotNull final String message, @NotNull final ServerChat.Channel channel) {
        this.player = player;
        this.message = message;
        this.channel = channel;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public ServerChat.Channel getChannel() {
        return this.channel;
    }

    @Override
    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String getCancelReason() {
        return this.cancelReason;
    }

    @Override
    public void setCancelReason(String reason) {
        this.cancelReason = reason;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean isCancelled) {
        this.cancelled = isCancelled;
    }

    @Override
    public ServerPlayerEntity getPlayer() {
        return this.player;
    }

    @Override
    public OnlineUser getUser() {
        return KiloServer.getServer().getOnlineUser(this.player);
    }

    @Override
    public ServerWorld getWorld() {
        return this.player.getServerWorld();
    }
}
