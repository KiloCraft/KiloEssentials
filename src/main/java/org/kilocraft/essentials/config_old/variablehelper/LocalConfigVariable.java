package org.kilocraft.essentials.config_old.variablehelper;

import java.util.HashMap;

public interface LocalConfigVariable {

    String getPrefix();

    HashMap<String, String> variables();
}
