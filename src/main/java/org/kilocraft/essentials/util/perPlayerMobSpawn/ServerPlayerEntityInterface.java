package org.kilocraft.essentials.util.perPlayerMobSpawn;

import net.minecraft.server.network.ServerPlayerEntity;

public interface ServerPlayerEntityInterface {

    PooledHashSets.PooledObjectLinkedOpenHashSet<ServerPlayerEntity> getCachedSingleMobDistanceMap();

    public int[] getMobCounts();
}
