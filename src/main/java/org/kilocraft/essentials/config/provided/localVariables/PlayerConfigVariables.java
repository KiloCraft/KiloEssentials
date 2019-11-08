package org.kilocraft.essentials.config.provided.localvariables;

import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;

public class PlayerConfigVariables implements LocalConfigVariable {
    private ServerPlayerEntity player;

    public PlayerConfigVariables(ServerPlayerEntity playerEntity) {
        this.player = playerEntity;
    }

    @Override
    public String getPrefix() {
        return "PLAYER";
    }

    @Override
    public HashMap<String, String> variables() {
        return new HashMap<String, String>(){{
            put("NAME", player.getName().asString());
        }};
    }
}
