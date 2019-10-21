package org.kilocraft.essentials.craft.chat;

import org.kilocraft.essentials.api.chat.TextFormat;

public class ChatMessage {
    private String original;
    private String formatted;
    private boolean formatText;

    public ChatMessage(String message, boolean formatText) {
        this.original = message;
        this.formatText = formatText;
        this.formatted = formatText ?
                TextFormat.translateAlternateColorCodes('&', message) :
                TextFormat.removeAlternateColorCodes('&', message);
    }

    public String getFormattedMessage() {
        return this.formatted;
    }

    public String getOriginal() {
        return this.original;
    }

    public void setMessage(String string) {
        this.original = string;
        this.formatted = formatText ?
                TextFormat.translateAlternateColorCodes('&', string) :
                TextFormat.removeAlternateColorCodes('&', string);
    }

}
