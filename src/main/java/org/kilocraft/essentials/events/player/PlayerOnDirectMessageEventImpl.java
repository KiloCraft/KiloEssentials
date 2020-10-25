package org.kilocraft.essentials.events.player;

import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.api.event.player.PlayerOnDirectMessageEvent;
import org.kilocraft.essentials.api.user.OnlineUser;

public class PlayerOnDirectMessageEventImpl implements PlayerOnDirectMessageEvent {
    private final ServerCommandSource source;
    private final OnlineUser receiver;
    private String message;
    private boolean cancelled = false;
    private String cancelReason;

    public PlayerOnDirectMessageEventImpl(@NotNull final ServerCommandSource source, @NotNull final OnlineUser receiver,  @NotNull final String message) {
        this.source = source;
        this.receiver = receiver;
        this.message = message;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public ServerCommandSource getSource() {
        return this.source;
    }

    @Override
    public OnlineUser getReceiver() {
        return this.receiver;
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

}
