package org.kilocraft.essentials.patch.optimizedSpawning;

import net.minecraft.server.network.ServerPlayerEntity;

public interface ServerPlayerEntityInterface {

    PooledHashSets.PooledObjectLinkedOpenHashSet<ServerPlayerEntity> getCachedSingleMobDistanceMap();

}
