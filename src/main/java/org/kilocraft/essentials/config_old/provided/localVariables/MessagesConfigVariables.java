package org.kilocraft.essentials.config_old.provided.localVariables;

import org.kilocraft.essentials.config_old.variablehelper.LocalConfigVariable;
import org.kilocraft.essentials.config_old.KiloConfigOLD;

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
        return KiloConfigOLD.getFileConfigOfMessages().getOrElse(s, "NULL");
    }
}
