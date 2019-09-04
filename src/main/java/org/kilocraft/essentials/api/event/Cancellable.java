package org.kilocraft.essentials.api.event;

/**
 * Marks an {@link Event} as cancellable.
 */
public interface Cancellable extends Event {

    /**
     * Returns true if the event is cancelled.
     *
     * @return Is the event cancelled?
     */
    boolean isCancelled();

    /**
     * Sets the event cancellation status
     *
     * @param isCancelled Will the event be cancelled?
     */
    void setCancelled(boolean isCancelled);

}