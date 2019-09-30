package org.kilocraft.essentials.api.event.eventImpl.serverEventsImpl;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.event.serverEvents.ServerEvent$OnCommandExecution;

public class ServerEvent$OnCommandExecutionImpl implements ServerEvent$OnCommandExecution {

    private ServerCommandSource source;
    private String command;

    public ServerEvent$OnCommandExecutionImpl(ServerCommandSource source, String command) {
        this.command = command;
        this.source = source;
    }

    @Override
    public String getCommand() {
        return this.command;
    }

    @Override
    public ServerCommandSource getSource() {
        return source;
    }

    @Override
    public MinecraftServer getServer() {
        return source.getMinecraftServer();
    }
}
