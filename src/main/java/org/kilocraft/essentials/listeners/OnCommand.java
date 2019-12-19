package org.kilocraft.essentials.listeners;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.commands.OnCommandExecutionEvent;
import org.kilocraft.essentials.chat.ServerChat;
import org.kilocraft.essentials.config.ConfigValueGetter;
import org.kilocraft.essentials.config.KiloConfig;

public class OnCommand implements EventHandler<OnCommandExecutionEvent> {
    @Override
    public void handle(OnCommandExecutionEvent event) {
        try {
            ServerPlayerEntity player = event.getExecutor().getPlayer();
            String command = event.getCommand().startsWith("/") ? event.getCommand().substring(1) : event.getCommand();
            if (KiloConfig.getProvider().getMain().getBooleanSafely("commandSpy.saveToLog", true)) KiloEssentials.getLogger().info(player.getGameProfile().getName() + " executed " + command);
            ServerChat.sendCommandSpy(event.getExecutor(), command);
        } catch (CommandSyntaxException e) {
            // If its not a player we dont want it
        }
    }
}
