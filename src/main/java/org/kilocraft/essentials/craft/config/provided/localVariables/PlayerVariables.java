package org.kilocraft.essentials.craft.config.provided.localVariables;

import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;

public class PlayerVariables implements LocalConfigVariable {
    private ServerPlayerEntity player;

    public PlayerVariables(ServerPlayerEntity playerEntity) {
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
            put("DISPLAYNAME", player.getDisplayName().getString());
        }};
    }
}
