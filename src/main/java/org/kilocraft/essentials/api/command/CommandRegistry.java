package org.kilocraft.essentials.api.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Map;

public interface CommandRegistry {
    /**
     * @return the dispatcher instance
     */
    CommandDispatcher<ServerCommandSource> getDispatcher();

    /**
     * Adds/Removes a command from the list
     * @param commandClass
     */

    void addCommand(KiloCommand commandClass);
    void removeCommand(KiloCommand commandClass);

    /**
     * Registers the commands
     * This method is uses internally
     *
     * @param c Command class
     * @param <C> Command type
     * @return the modified command
     */

    <C extends KiloCommand> C register(C c);

    /**
     * unRegisters the commands
     * This method is uses internally
     *
     * @param c Command class
     * @param <C> Command type
     * @return the modified command
     */

    <C extends KiloCommand> C unregister(C c);

    /**
     * Registers all the commands in the command registry
     */

    void registerAll();

    /**
     * unRegisters all the commands in the command registry
     */

    void unregisterAll();

    /**
     * @return an instance of the command handlers map
     */

    Map getHandlers();
}
