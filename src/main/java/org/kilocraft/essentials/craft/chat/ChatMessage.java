package org.kilocraft.essentials.craft.chat;

import net.minecraft.text.LiteralText;
import org.kilocraft.essentials.api.chat.TextFormat;

public class ChatMessage {
    private String original;
    private LiteralText formatted;

    public ChatMessage(String message, boolean formatText) {
        this.original = message;
        this.formatted = new LiteralText(formatText ? TextFormat.translateAlternateColorCodes('&', message) : message);
    }

    public LiteralText getFormattedMessage() {
        return this.formatted;
    }

    public String getFormattedAsString() {
        return this.formatted.asString();
    }

    public String getOriginal() {
        return this.original;
    }

}
