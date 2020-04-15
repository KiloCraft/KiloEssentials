package org.kilocraft.essentials.chat;

import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.api.text.MessageReceptionist;

public class LoggedMessage extends TextMessage {
    private MessageReceptionist receptionist;

    public LoggedMessage(@NotNull final MessageReceptionist receptionist, @NotNull final String message, boolean format) {
        super(message, format);
        this.receptionist = receptionist;
    }

    public LoggedMessage(@NotNull final MessageReceptionist receptionist, @NotNull final String message) {
        this(receptionist, message, true);
    }

    public MessageReceptionist getReceptionist() {
        return this.receptionist;
    }
}
