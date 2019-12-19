package org.kilocraft.essentials.listeners;

import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.commands.OnCommandExecutionEvent;
import org.kilocraft.essentials.chat.ServerChat;
import org.kilocraft.essentials.commands.CommandHelper;
import org.kilocraft.essentials.config.KiloConfig;

public class OnCommand implements EventHandler<OnCommandExecutionEvent> {
    @Override
    public void handle(OnCommandExecutionEvent event) {
        if (CommandHelper.isPlayer(event.getExecutor())) {
            String command = event.getCommand().startsWith("/") ? event.getCommand().substring(1) : event.getCommand();
            ServerChat.sendCommandSpy(event.getExecutor(), command);

            if (KiloConfig.getProvider().getMain().getBooleanSafely("commandSpy.saveToLog", true))
                KiloServer.getLogger().info("[" + event.getExecutor().getName() + "]: " + command);
        }
    }
}
