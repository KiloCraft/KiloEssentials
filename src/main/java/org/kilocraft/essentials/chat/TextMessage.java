package org.kilocraft.essentials.chat;

import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.kilocraft.essentials.api.text.TextFormat;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.User;
import org.kilocraft.essentials.config.ConfigVariableFactory;

public class TextMessage {
    private String original;
    private String formatted;
    private boolean formatText;

    public TextMessage(String message, boolean formatText) {
        this.original = message;
        this.formatText = formatText;
        this.formatted = formatText ?
                TextFormat.translateAlternateColorCodes('&', message) :
                TextFormat.removeAlternateColorCodes('&', message);
    }

    public TextMessage(String message, User user) {
        this.original = ConfigVariableFactory.replaceUserVariables(message, user);
        this.formatText = true;
        this.formatted = TextFormat.translateAlternateColorCodes('&', original);
    }

    public TextMessage(String message, OnlineUser user) {
        this.original = ConfigVariableFactory.replaceOnlineUserVariables(message, user);
        this.formatText = true;
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
        this.formatted = format ?
                TextFormat.translateAlternateColorCodes('&', string) :
                TextFormat.removeAlternateColorCodes('&', string);
    }

    public Text toText() {
        return new LiteralText(this.formatted);
    }

    public Text toComponent() {
        return toText();
    }

}
