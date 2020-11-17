package org.kilocraft.essentials.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import org.kilocraft.essentials.api.world.MonitorableWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin implements MonitorableWorld {

    @Shadow public abstract ServerChunkManager getChunkManager();

    @Shadow @Final private List<ServerPlayerEntity> players;

    @Override
    public int cachedChunks() {
        if (this.getChunkManager() == null)
            return -1;

        return this.getChunkManager().getLoadedChunkCount();
    }

    @Override
    public int totalLoadedChunks() {
        if (this.getChunkManager() == null)
            return -1;

        return this.getChunkManager().getTotalChunksLoadedCount();
    }

    @Override
    public int players() {
        if (this.players == null)
            return -1;

        return this.players.size();
    }

}
