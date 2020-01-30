package org.kilocraft.essentials.config.variablehelper;

import com.electronwill.nightconfig.core.file.FileConfig;

import java.util.concurrent.atomic.AtomicReference;

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

    public final <L extends LocalConfigVariable> String replace(String input, L... localFormatters) {
        AtomicReference<String> string = new AtomicReference<>(input);
        for (L localFormatter : localFormatters) {
            localFormatter.variables().forEach((name, value) ->
                    string.set(string.get().replaceAll("%" + localFormatter.getPrefix() + "_" + name + "%", value)));
        }

        return string.get();
    }


}
