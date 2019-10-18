package org.kilocraft.essentials.craft.chat;

import org.kilocraft.essentials.api.chat.TextColor;

public class ChatMessage {
    private String message;

    public ChatMessage(String message, boolean colorCodes) {
        this.message = colorCodes ? TextColor.translateAlternateColorCodes('&', message) : message;
    }

    public String getMessage() {
        return this.message;
    }

}
