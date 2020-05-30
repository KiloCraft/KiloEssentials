package org.kilocraft.essentials.api.event.context;

import org.kilocraft.essentials.api.event.Cancellable;

/**
 * Involves a cancellable context, where a failure reason may be present.
 */
public interface CancellableReasonContext extends Cancellable, Contextual {

    /**
     * Gets the cancellation reason
     *
     * @return the cancellation reason
     */
    String getCancelReason();

    /**
     * Sets the cancellation reason
     * <p>
     * This will be the text shown to user when
     * this event gets cancelled.
     * 
     * This will implicitly call {@link Cancellable#setCancelled(boolean)} when you set the cancel reason.
     *
     * @param reason The cancellation reason
     */
    void setCancelReason(String reason);
}
