package org.kilocraft.essentials.events.commands;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.event.commands.OnCommandExecutionEvent;

public class OnCommandExecutionEventImpl implements OnCommandExecutionEvent {

    private final ServerCommandSource source;
    private final String command;
    private boolean isCanceled;

    public OnCommandExecutionEventImpl(ServerCommandSource source, String command) {
        this.command = command;
        this.source = source;
    }

    @Override
    public String getCommand() {
        return this.command;
    }

    @Override
    public ServerCommandSource getExecutor() {
        return source;
    }

    @Override
    public MinecraftServer getServer() {
        return source.getServer();
    }

    @Override
    public boolean isCancelled() {
        return this.isCanceled;
    }

    @Override
    public void setCancelled(boolean isCancelled) {
        this.isCanceled = isCancelled;
    }

}
