package org.kilocraft.essentials.patch.perPlayerMobSpawn;

import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.network.ServerPlayerEntity;

public interface ThreadedAnvilChunkStorageInterface {

    PlayerMobDistanceMap getMobDistanceMap();

    public void updatePlayerMobTypeMap(Entity entity);

    public int getMobCountNear(ServerPlayerEntity player, SpawnGroup spawnGroup);

}
