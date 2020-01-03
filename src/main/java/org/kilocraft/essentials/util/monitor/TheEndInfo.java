package org.kilocraft.essentials.util.monitor;

import net.minecraft.server.world.ServerWorld;
import org.kilocraft.essentials.api.world.MonitorableWorld;

public class TheEndInfo implements MonitorableWorld {
    private ServerWorld world;

    public TheEndInfo(ServerWorld world) {
        this.world = world;
    }

    @Override
    public int cachedChunks() {
        return world.getChunkManager().getLoadedChunkCount();
    }

    @Override
    public int totalLoadedChunks() {
        return world.getChunkManager().getTotalChunksLoadedCount();
    }

    @Override
    public int loadedEntities() {
        return world.getEntities(null, entity -> true).size();
    }

    @Override
    public int players() {
        return world.getPlayers().size();
    }

    @Override
    public ServerWorld getWorld() {
        return world;
    }

}
