package org.kilocraft.essentials.config.localVariableHelper;

import com.electronwill.nightconfig.core.file.FileConfig;

public class LocalVariableReplaced {

    public LocalVariableReplaced() {
    }

    public <L extends LocalConfigVariable> String replace(String string, L localConfigVariable, FileConfig config) {
        final String[] result = {string};

        localConfigVariable.variables().forEach((key, value) -> {
            String valueToReplace = "%" + localConfigVariable.getPrefix() + "_" + key + "%";
            if (string.contains(valueToReplace))
                result[0] = string.replace(valueToReplace, value);
        });

        return result[0];
    }


}
