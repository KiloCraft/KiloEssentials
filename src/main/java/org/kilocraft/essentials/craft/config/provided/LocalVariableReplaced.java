package org.kilocraft.essentials.craft.config.provided;

import org.kilocraft.essentials.craft.config.provided.localVariables.LocalConfigVariable;

public class LocalVariableReplaced {

    public LocalVariableReplaced() {
    }

    public <L extends LocalConfigVariable> String replace(String string, L localConfigVariable) {
        final String[] result = {string};

        localConfigVariable.variables().forEach((key, value) -> {
            String valueToReplace = "%" + localConfigVariable.getPrefix() + "_" + key + "%";
            if (string.contains(valueToReplace))
                System.out.println("ValToRep: " +  valueToReplace + " RESULT: " + value);
                result[0] = string.replace(valueToReplace, value);
        });

        return result[0];
    }

}
