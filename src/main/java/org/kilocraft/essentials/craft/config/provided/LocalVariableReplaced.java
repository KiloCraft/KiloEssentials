package org.kilocraft.essentials.craft.config.provided;

import org.kilocraft.essentials.craft.config.provided.localVariables.LocalConfigVariable;

public class LocalVariableReplaced {

    public LocalVariableReplaced() {
    }

    public <L extends LocalConfigVariable> String replace(String string, L localConfigVariable) {
        String result = string;
        localConfigVariable.variables().forEach((key, value) -> {
            String valueToReplace = "%" + localConfigVariable.getPrefix() + "_" + key + "%";
            System.out.println("VAL TO REPLACE: " + valueToReplace);
            System.out.println("RE: " + key);
            result.replace(valueToReplace, value);
        });

        return result;
    }

}
