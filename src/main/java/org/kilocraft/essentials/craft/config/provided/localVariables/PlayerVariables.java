package org.kilocraft.essentials.craft.config.provided.localVariables;

import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;

public class PlayerVariables implements LocalConfigVariable {
    private ServerPlayerEntity player;
    private HashMap<String, String> hashMap = new HashMap<>();

    public PlayerVariables(ServerPlayerEntity playerEntity) {
        this.player = playerEntity;

        hashMap.put("NAME", this.player.getName().asString());
        hashMap.put("DISPLAYNAME", this.player.getDisplayName().asString());
    }

    @Override
    public String getPrefix() {
        return "PLAYER";
    }

    @Override
    public HashMap<String, String> variables() {
        return hashMap;
    }
}
