package org.kilocraft.essentials.config.provided.localVariables;

import org.kilocraft.essentials.config.variablehelper.LocalConfigVariable;
import org.kilocraft.essentials.user.ServerUser;

import java.util.HashMap;

// TODO This isn't used anymore.
public class UserConfigVariables implements LocalConfigVariable {
    private ServerUser serverUser;

    public UserConfigVariables(ServerUser serverUser) {
        this.serverUser = serverUser;
    }

    @Override
    public String getPrefix() {
        return "USER";
    }

    @Override
    public HashMap<String, String> variables() {
        return new HashMap<String, String>(){{
            put("NAME", serverUser.getUsername());
            put("DISPLAYNAME", serverUser.getDisplayName());
        }};
    }

}
