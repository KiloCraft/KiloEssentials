package org.kilocraft.essentials.api.command;

import java.util.Map;

public interface CommandRegistry {
    /**
     * This method registers the command to the CommandRegistry
     * @param commndClass
     */

    void register(CommandRegistry commndClass);

    /**
     * Registers the commands
     * This method is uses internally
     *
     * @param e
     * @param <C>
     * @return
     */

    <C extends KiloCommand> C register(C e);

    Map getHandlers();
}
