package org.kilocraft.essentials.events;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class CommandEvents {

    public static final Event<CommandRegistration> REGISTER_COMMAND = EventFactory.createArrayBacked(CommandRegistration.class, (callbacks) -> (dispatcher, environment) -> {
        for (CommandRegistration callback : callbacks) {
            callback.register(dispatcher, environment);
        }
    });

    public interface CommandRegistration {
        void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandManager.RegistrationEnvironment environment);
    }

}
