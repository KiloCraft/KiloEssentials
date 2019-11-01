package org.kilocraft.essentials.craft.config.provided;

import org.kilocraft.essentials.craft.config.provided.localvariables.LocalConfigVariable;

public class LocalVariableReplaced {

    public LocalVariableReplaced() {
    }

    public <L extends LocalConfigVariable> String replace(String string, L localConfigVariable) {
        final String[] result = {string};

        localConfigVariable.variables().forEach((key, value) -> {
            String valueToReplace = "%" + localConfigVariable.getPrefix() + "_" + key + "%";
            if (string.contains(valueToReplace))
                result[0] = string.replace(valueToReplace, value);
        });

        return result[0];
    }

//    public <L extends LocalConfigVariable> String replace(String string, L localConfigVariable) {
//        StringBuilder builder = new StringBuilder();
//
//        localConfigVariable.variables().forEach((key, value) -> {
//            String valueToReplace = "%" + localConfigVariable.getPrefix() + "_" + key + "%";
//                result = string.replace(valueToReplace, value);
//        });
//
//        return builder.toString();
//    }

}
