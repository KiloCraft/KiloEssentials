package org.kilocraft.essentials.api.event.context;

import net.minecraft.text.Text;

/**
 * Represents a context which involves a message.
 */
public interface MessageContext extends Contextual {

    /**
     * Gets the message that has been sent
     *
     * @return The events message
     */
    Text getMessage();

    /**
     * Sets the message
     *
     * @param message The new message
     */
    void setMessage(Text message);
}
