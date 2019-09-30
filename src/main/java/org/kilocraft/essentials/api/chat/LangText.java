package org.kilocraft.essentials.api.chat;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import org.kilocraft.essentials.api.Mod;
import org.kilocraft.essentials.api.util.CommandSender;

public class LangText {

    public static LiteralText getFormatter (boolean allowColorCodes, String key, Object... objects) {
        String lang = Mod.getLang().getProperty(key);
        LiteralText literalText = new LiteralText ("");
        String result = null;

        if (objects[0] != null) {
            result = String.format(lang, objects);
        }

        if (allowColorCodes) {
            result = ChatColor.translateAlternateColorCodes('&', result);
        } else {
            result = ChatColor.removeAlternateColorCodes('&', result);
        }


        literalText.append(result);
        return literalText;
    }

    public static LiteralText get(boolean allowColorCodes, String key) {
        String lang = Mod.getLang().getProperty(key);
        LiteralText literalText = new LiteralText ("");
        String result = lang;

        if (allowColorCodes) {
            result = ChatColor.translateAlternateColorCodes('&', result);
        } else {
            result = ChatColor.removeAlternateColorCodes('&', result);
        }

        literalText.append(result);
        return literalText;
    }

    public static void sendToUniversalSource(ServerCommandSource source, String key, boolean log) {
        String text = Mod.getLang().getProperty(key);
        LiteralText literalText;
        if (CommandSender.isConsole(source)) {
            literalText = ChatColor.removeAlternateToLiteralText('&', text);
        } else {
            literalText = ChatColor.translateToLiteralText('&', text);
        }

        source.sendFeedback(literalText, log);
    }


    public static void sendToUniversalSource(ServerCommandSource source, String key, boolean log, Object... objects) {
        String result = null;
        String lang = Mod.getLang().getProperty(key);
        if (objects[0] != null) {
            result = String.format(lang, objects);
        }
        LiteralText literalText;
        if (CommandSender.isConsole(source)) {
            literalText = new LiteralText(ChatColor.removeAlternateColorCodes('&', result));
        } else {
            literalText = new LiteralText(ChatColor.translateAlternateColorCodes('&', result));
        }

        source.sendFeedback(literalText, log);
    }
}
