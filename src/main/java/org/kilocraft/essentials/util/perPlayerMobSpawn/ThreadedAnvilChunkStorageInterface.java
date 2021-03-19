package org.kilocraft.essentials.util.perPlayerMobSpawn;

import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.network.ServerPlayerEntity;

public interface ThreadedAnvilChunkStorageInterface {

    PlayerMobDistanceMap getMobDistanceMap();

    public void updatePlayerMobTypeMap(Entity entity);

    public int getMobCountNear(ServerPlayerEntity player, SpawnGroup spawnGroup);

}
