package org.kilocraft.essentials.api.event;

/**
 * This is the event handler class all your event handlers should implement.
 * See {@link EventRegistry} for information on how to use event handlers
 *
 * @param <T> The event you want to handle
 */
public interface EventHandler<T> {

    void handle(T event);

}