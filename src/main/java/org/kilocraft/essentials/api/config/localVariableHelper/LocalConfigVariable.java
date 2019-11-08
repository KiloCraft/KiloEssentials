package org.kilocraft.essentials.api.config.localVariableHelper;

import java.util.HashMap;

public interface LocalConfigVariable {

    String getPrefix();

    HashMap<String, String> variables();
}
