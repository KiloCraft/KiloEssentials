package org.kilocraft.essentials.utils;

import net.minecraft.text.LiteralText;
import org.kilocraft.essentials.Mod;

public class LangText {

    public static LiteralText getFormatter (boolean AllowColorCodes, String key, Object... objects) {
        String lang = Mod.lang.getProperty(key);
        LiteralText literalText = new LiteralText ("");
        String result = null;
        Object tmpObject;

        for (int i = 0; i < objects.length; i++) {
            Object object = objects[i];
            result = lang.replaceFirst("%s", object.toString());
            if (AllowColorCodes) {
                result = ChatColor.translateAlternateColorCodes('&', result);
            }
        }

        literalText.append(result);
        return literalText;
    }

}
