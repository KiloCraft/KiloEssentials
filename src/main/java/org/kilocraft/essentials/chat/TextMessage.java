package org.kilocraft.essentials.chat;

import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import org.kilocraft.essentials.api.text.TextFormat;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.User;
import org.kilocraft.essentials.config.ConfigVariableFactory;
import org.kilocraft.essentials.util.text.Texter;

public class TextMessage {
    private String original;
    private String formatted;
    private boolean allowFormats;

    public TextMessage(String message) {
        this(message, true);
    }

    public TextMessage(String message, boolean allowFormats) {
        this.original = message;
        this.allowFormats = allowFormats;
        this.formatted = allowFormats ?
                TextFormat.translateAlternateColorCodes('&', message) :
                TextFormat.removeAlternateColorCodes('&', message);
    }

    public TextMessage(String message, User user) {
        this.original = ConfigVariableFactory.replaceUserVariables(message, user);
        this.allowFormats = true;
        this.formatted = TextFormat.translateAlternateColorCodes('&', original);
    }

    public TextMessage(String message, OnlineUser user) {
        this.original = ConfigVariableFactory.replaceOnlineUserVariables(message, user);
        this.allowFormats = true;
        this.formatted = TextFormat.translateAlternateColorCodes('&', message);
    }

    public String getFormattedMessage() {
        return this.formatted;
    }

    public String getOriginal() {
        return this.original;
    }

    public void setMessage(String string, boolean format) {
        this.original = string;
        this.allowFormats = format;
        formatMessage();
    }

    public void setMessage(String string) {
        this.original = string;
        formatMessage();
    }

    public LiteralText toText() {
        return (LiteralText) Texter.newRawText(this.formatted);
    }

    public MutableText toComponent() {
        return this.toText();
    }

    private void formatMessage() {
        this.formatted = this.allowFormats ?
                TextFormat.translateAlternateColorCodes('&', this.original) :
                TextFormat.removeAlternateColorCodes('&', this.original);
    }

    @Override
    public String toString() {
        return this.original;
    }
}
