package org.kilocraft.essentials.patch.perPlayerMobSpawn;

import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.network.ServerPlayerEntity;

public interface ThreadedAnvilChunkStorageInterface {

    PlayerMobDistanceMap getMobDistanceMap();

    void updatePlayerMobTypeMap(Entity entity);

    int getMobCountNear(ServerPlayerEntity player, SpawnGroup spawnGroup);

}
