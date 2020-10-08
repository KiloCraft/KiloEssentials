package org.kilocraft.essentials.chat;

import net.kyori.adventure.text.Component;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.api.text.TextFormat;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.User;
import org.kilocraft.essentials.config.ConfigVariableFactory;
import org.kilocraft.essentials.util.text.Texter;

@Deprecated
public class MutableTextMessage {
    private String original;
    private Component formatted;
    private boolean allowFormats;

    public MutableTextMessage(String message) {
        this(message, true);
    }

    public MutableTextMessage(String message, boolean allowFormats) {
        this.original = message;
        this.allowFormats = allowFormats;
        this.formatted = allowFormats ?
                ComponentText.of(message) :
                ComponentText.removeStyle(ComponentText.of(message));
    }

    public MutableTextMessage(String message, User user) {
        this.original = ConfigVariableFactory.replaceUserVariables(message, user);
        this.allowFormats = true;
        this.formatted = ComponentText.of(message);
    }

    public MutableTextMessage(String message, OnlineUser user) {
        this.original = ConfigVariableFactory.replaceOnlineUserVariables(message, user);
        this.allowFormats = true;
        this.formatted = ComponentText.of(message);
    }

    @Deprecated
    public String getFormattedMessage() {
        return this.formatted.toString();
    }

    public String getOriginal() {
        return this.original;
    }

    public void setMessage(String string) {
        setMessage(string, this.allowFormats);
    }

    public void setMessage(String string, boolean format) {
        this.original = string;
        this.allowFormats = format;
        formatMessage();
    }

    public LiteralText toText() {
        return (LiteralText) ComponentText.toText(this.formatted);
    }

    public Component getComponent() {
        return this.formatted ;
    }

    private void formatMessage() {
        this.formatted = this.allowFormats ?
                ComponentText.of(this.original) :
                ComponentText.removeStyle(ComponentText.of(this.original));
    }

    @Override
    public String toString() {
        return this.original;
    }
}
