package org.kilocraft.essentials.config.provided.localVariables;

import org.kilocraft.essentials.api.config.localVariableHelper.LocalConfigVariable;
import org.kilocraft.essentials.config.KiloConfig;

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
        return KiloConfig.getFileConfigOfMessages().getOrElse(s, "NULL");
    }
}
