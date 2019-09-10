package org.kilocraft.essentials.api.event;

import java.util.Map;

/**
 * This is the global event registry class.
 *
 * You shouldn't use it directly, but use the {@link EventHandler}
 * or {@link EventHandler} methods instead.
 */
public interface EventRegistry {

    /**
     * This method registers your events to this event manager
     *
     * @param eventClass Your event handler class
     */
    void register(EventHandler eventClass);

    /**
     * Trigger an event.
     * While this is mostly used internally, you can use it to trigger your custom events.
     *
     * @param e   Event class
     * @param <E> Event type
     * @return The modified event
     */
    <E extends Event> E trigger(E e);

    Map getHandlers();

}
