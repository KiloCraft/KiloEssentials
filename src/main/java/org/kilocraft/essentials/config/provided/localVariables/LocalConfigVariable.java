package org.kilocraft.essentials.config.provided.localVariables;

import java.util.HashMap;

public interface LocalConfigVariable {

    String getPrefix();

    HashMap<String, String> variables();
}
