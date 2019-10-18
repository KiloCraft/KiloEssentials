package org.kilocraft.essentials.craft.config.provided.localVariables;

import java.util.HashMap;

public interface LocalConfigVariable {

    String getPrefix();

    HashMap<String, String> variables();
}
