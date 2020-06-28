package org.kilocraft.essentials.api.world;

public interface MonitorableWorld {
    int cachedChunks();

    int totalLoadedChunks();

    int loadedEntities();

    int players();
}
