package org.kilocraft.essentials.api.chat;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.commands.CommandHelper;

public class LangText {

    public static LiteralText getFormatter(boolean allowColorCodes, String key, Object... objects) {
        String lang = ModConstants.getLang().getProperty(key);
        LiteralText literalText = new LiteralText ("");
        String result = "";

        if (objects[0] != null) {
            result = String.format(lang, objects);
        }

        if (allowColorCodes) result = TextFormat.translateAlternateColorCodes('&', result);
        else result = TextFormat.removeAlternateColorCodes('&', result);


        literalText.append(result);
        return literalText;
    }

    public static LiteralText get(boolean allowColorCodes, String key) {
        String lang = ModConstants.getLang().getProperty(key);
        LiteralText literalText = new LiteralText ("");
        String result = lang;

        if (allowColorCodes) result = TextFormat.translateAlternateColorCodes('&', result);
        else result = TextFormat.removeAlternateColorCodes('&', result);

        literalText.append(result);
        return literalText;
    }

    public static void sendToUniversalSource(ServerCommandSource source, String key, boolean log, Object... objects) {
        String result = "";
        String lang = ModConstants.getLang().getProperty(key);
        if (objects[0] != null) {
            result = String.format(lang, objects);
        }
        LiteralText literalText;
        if (CommandHelper.isConsole(source)) {
            literalText = new LiteralText(TextFormat.removeAlternateColorCodes('&', result));
        } else {
            literalText = new LiteralText(TextFormat.translateAlternateColorCodes('&', result));
        }

        source.sendFeedback(literalText, log);
    }
}
