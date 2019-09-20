package org.kilocraft.essentials.api.command;

import java.util.ArrayList;

public interface IKiloCommand {
    void getCommand();

    /**
     * @return command usage string
     */

    String getUsage();

    /**
     * @return a List of the command aliases
     */

    ArrayList<String> getAliases();
}
