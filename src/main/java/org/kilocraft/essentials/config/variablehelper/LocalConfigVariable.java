package org.kilocraft.essentials.config.variablehelper;

import java.util.HashMap;

public interface LocalConfigVariable {

    String getPrefix();

    HashMap<String, String> variables();
}
