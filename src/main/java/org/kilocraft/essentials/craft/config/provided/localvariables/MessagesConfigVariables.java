package org.kilocraft.essentials.craft.config.provided.localvariables;

import org.kilocraft.essentials.craft.config.KiloConifg;

import java.util.HashMap;

public class MessagesConfigVariables implements LocalConfigVariable {

    @Override
    public String getPrefix() {
        return "general";
    }

    @Override
    public HashMap<String, String> variables() {
        return new HashMap<String, String>(){{
            put("PREFIX", getValue("general.prefix"));
        }};
    }

    private static String getValue(String s) {
        return KiloConifg.getFileConfigOfMessages().getOrElse(s, "NULL");
    }
}
