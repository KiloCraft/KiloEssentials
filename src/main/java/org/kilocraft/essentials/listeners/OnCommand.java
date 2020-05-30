package org.kilocraft.essentials.listeners;

import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.commands.OnCommandExecutionEvent;
import org.kilocraft.essentials.chat.ServerChat;
import org.kilocraft.essentials.commands.CommandUtils;
import org.kilocraft.essentials.config.KiloConfig;

public class OnCommand implements EventHandler<OnCommandExecutionEvent> {
    @Override
    public void handle(@NotNull OnCommandExecutionEvent event) {
        if (CommandUtils.isPlayer(event.getExecutor())) {
            String command = event.getCommand().startsWith("/") ? event.getCommand().substring(1) : event.getCommand();

            boolean isIgnored = false;
            for (String cmd : KiloConfig.main().ignoredCommandsForLogging) {
                if (command.startsWith(cmd)) {
                    isIgnored = true;
                    break;
                }
            }

            if (!isIgnored) {
                ServerChat.sendCommandSpy(event.getExecutor(), command);

                if (KiloConfig.main().server().logCommands) {
                    KiloServer.getLogger().info("[" + event.getExecutor().getName() + "]: " + command);
                }
            }
        }
    }
}
