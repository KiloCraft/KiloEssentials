package org.kilocraft.essentials.patch.perPlayerMobSpawn;

import net.minecraft.server.network.ServerPlayerEntity;

public interface ServerPlayerEntityInterface {

    PooledHashSets.PooledObjectLinkedOpenHashSet<ServerPlayerEntity> getCachedSingleMobDistanceMap();

    public int[] getMobCounts();
}
