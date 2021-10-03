package org.kilocraft.essentials.simplecommand;

import org.kilocraft.essentials.api.command.SimpleExecutableCommand;

public class SimpleCommand {
    String label;
    String permReq;
    int opReq;
    boolean hasArgs;
    SimpleExecutableCommand executable;

    public SimpleCommand(String label, SimpleExecutableCommand executable) {
        this.label = label;
        this.hasArgs = true;
        this.executable = executable;
    }

    public String getLabel() {
        return this.label;
    }

    public SimpleCommand requires(String permission) {
        this.permReq = permission;
        return this;
    }

    public SimpleCommand requires(int opLevel) {
        this.opReq = opLevel;
        return this;
    }

    public SimpleCommand withoutArgs() {
        this.hasArgs = false;
        return this;
    }
}
