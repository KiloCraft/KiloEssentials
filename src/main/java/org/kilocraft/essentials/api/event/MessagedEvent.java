package org.kilocraft.essentials.api.event;

import net.minecraft.text.LiteralText;

public interface MessagedEvent {

    /**
     * Gets the message that has been sent
     *
     * @return The chat message
     */
    LiteralText getMessage();

    /**
     * Sets the message
     *
     * @param message The new message
     */
    void setMessage(LiteralText message);
}
