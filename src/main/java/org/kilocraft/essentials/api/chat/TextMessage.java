package org.kilocraft.essentials.api.chat;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import org.kilocraft.essentials.api.util.CommandSender;

public class TextMessage {
    public TextMessage() {
    }

    public static void sendToUniversalSource(ServerCommandSource source, String text, boolean log) {
        LiteralText literalText;
        if (CommandSender.isConsole(source)) {
            literalText = TextColor.removeAlternateToLiteralText('&', text);
        } else {
            literalText = TextColor.translateToLiteralText('&', text);
        }

        source.sendFeedback(literalText, log);
    }

    public static void sendToUniversalSource(ServerCommandSource source, LiteralText text, boolean log) {
        LiteralText literalText;
        if (CommandSender.isConsole(source)) {
            literalText = TextColor.removeAlternateToLiteralText('&', text.getString());
        } else {
            literalText = TextColor.translateToLiteralText('&', text.getString());
        }

        source.sendFeedback(literalText, log);
    }

}
