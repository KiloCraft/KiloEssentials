package org.kilocraft.essentials.craft.chat;

import net.minecraft.text.LiteralText;
import org.kilocraft.essentials.api.chat.TextFormat;

public class ChatMessage {
    private String original;
    private LiteralText formatted;
    private boolean formatText;

    public ChatMessage(String message, boolean formatText) {
        this.original = message;
        this.formatText = formatText;
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

    public void setMessage(String string) {
        this.original = string;
        this.formatted = new LiteralText(formatText ? TextFormat.translateAlternateColorCodes('&', string) : string);
    }

}
