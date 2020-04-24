package org.kilocraft.essentials.simplecommand;

import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.command.SimpleExecutableCommand;

public class SimpleCommand {
    String id;
    String label;
    String permReq;
    int opReq;
    SimpleExecutableCommand executable;

    public SimpleCommand(String id, String label, SimpleExecutableCommand executable) {
        this.id = id;
        this.label = label;
        this.executable = executable;
    }

    public String getLabel() {
        return this.label;
    }

    public String getId() {
        return this.id;
    }

    public SimpleCommand requires(String permission) {
        this.permReq = permission;
        return this;
    }

    public SimpleCommand requires(int opLevel) {
        this.opReq = opLevel;
        return this;
    }

    @Nullable
    public String getRequirementPerm() {
        return this.permReq;
    }

    public int getRequirementLevel() {
        return this.opReq;
    }

}
