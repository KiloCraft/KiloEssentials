package org.kilocraft.essentials.api.world;

import net.minecraft.server.world.ServerWorld;

public interface MonitorableWorld {
    int cachedChunks();

    int totalLoadedChunks();

    int loadedEntities();

    int players();

    ServerWorld getWorld();
}
