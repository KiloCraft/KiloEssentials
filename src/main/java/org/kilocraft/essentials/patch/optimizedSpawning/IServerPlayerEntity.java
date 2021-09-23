package org.kilocraft.essentials.patch.optimizedSpawning;

import net.minecraft.server.network.ServerPlayerEntity;

public interface IServerPlayerEntity {

    PooledHashSets.PooledObjectLinkedOpenHashSet<ServerPlayerEntity> getCachedSingleMobDistanceMap();

}
