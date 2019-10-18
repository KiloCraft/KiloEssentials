package org.kilocraft.essentials.api.chat;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import org.kilocraft.essentials.api.util.CommandHelper;

public class TextMessage {
    public TextMessage() {
    }

    public static void sendToUniversalSource(ServerCommandSource source, String text, boolean log) {
        LiteralText literalText;
        if (CommandHelper.isConsole(source)) {
            literalText = TextFormat.removeAlternateToLiteralText('&', text);
        } else {
            literalText = TextFormat.translateToLiteralText('&', text);
        }

        source.sendFeedback(literalText, log);
    }

    public static void sendToUniversalSource(ServerCommandSource source, LiteralText text, boolean log) {
        LiteralText literalText;
        if (CommandHelper.isConsole(source)) {
            literalText = TextFormat.removeAlternateToLiteralText('&', text.getString());
        } else {
            literalText = TextFormat.translateToLiteralText('&', text.getString());
        }

        source.sendFeedback(literalText, log);
    }

}
