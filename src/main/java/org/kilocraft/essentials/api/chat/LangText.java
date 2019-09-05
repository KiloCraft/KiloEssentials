package org.kilocraft.essentials.api.chat;

import net.minecraft.text.LiteralText;
import org.kilocraft.essentials.api.Mod;

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

}
