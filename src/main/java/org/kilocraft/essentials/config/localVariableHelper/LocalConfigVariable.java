package org.kilocraft.essentials.config.localVariableHelper;

import java.util.HashMap;

public interface LocalConfigVariable {

    String getPrefix();

    HashMap<String, String> variables();
}
