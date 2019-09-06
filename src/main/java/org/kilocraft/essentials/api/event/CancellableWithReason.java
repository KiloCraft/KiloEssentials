package org.kilocraft.essentials.api.event;

public interface CancellableWithReason extends Cancellable {

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
     * @param reason The cancellation reason
     */
    void setCancelReason(String reason);

}
