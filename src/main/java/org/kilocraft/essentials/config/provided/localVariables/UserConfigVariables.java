package org.kilocraft.essentials.config.provided.localvariables;

import org.kilocraft.essentials.user.User;

import java.util.HashMap;

public class UserConfigVariables implements LocalConfigVariable {
    private User user;

    public UserConfigVariables(User user) {
        this.user = user;
    }

    @Override
    public String getPrefix() {
        return "USER";
    }

    @Override
    public HashMap<String, String> variables() {
        return new HashMap<String, String>(){{
            put("NAME", user.getName());
            put("DISPLAYNAME", user.getDisplayNameAsString());
            put("NICKNAME", user.getNickname());
        }};
    }
}
