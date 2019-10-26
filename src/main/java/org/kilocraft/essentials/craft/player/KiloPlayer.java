package org.kilocraft.essentials.craft.player;

import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;

public class KiloPlayer {
    private UUID uuid;
    private String nickName;

    public KiloPlayer(ServerPlayerEntity serverPlayer) {
        this.uuid = serverPlayer.getUuid();
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public String getNickName() {
        return this.nickName;
    }

    public void setNickName(String name) {
        this.nickName = name;
    }
}
