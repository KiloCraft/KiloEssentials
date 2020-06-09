package org.kilocraft.essentials.api.event;

import org.jetbrains.annotations.NotNull;

/**
 * This is the event handler class all your event handlers should implement.
 * See {@link EventRegistry} for information on how to use event handlers
 *
 * @param <E> The event you want to handle
 */
public interface EventHandler<E extends Event> {

    /**
     * Handle the event
     *
     * @param event the event that's gonna be handled
     */
    void handle(@NotNull final E event);

}