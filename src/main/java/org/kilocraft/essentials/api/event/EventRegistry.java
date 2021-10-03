package org.kilocraft.essentials.api.event;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * This is the global event registry class.
 * <p>
 * You shouldn't use it directly, but use the {@link EventHandler}
 * or {@link EventHandler} methods instead.
 */
public interface EventRegistry {

    /**
     * This method registers your events to this event manager
     *
     * @param handlerClass Your event handler class
     */
    <E extends EventHandler<?>> void register(@NotNull final E handlerClass);

    /**
     * Trigger an event.
     * While this is mostly used internally, you can use it to trigger your custom events.
     *
     * @param e   Event class
     * @param <E> Event type
     * @return The modified event
     */
    <E extends Event> E trigger(E e);

    Map<String, List<EventHandler<?>>> getHandlers();

}
