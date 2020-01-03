package org.kilocraft.essentials.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import org.kilocraft.essentials.api.world.MonitorableWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin implements MonitorableWorld {

    @Shadow public abstract ServerChunkManager getChunkManager();

    @Shadow @Final private List<Entity> globalEntities;

    @Shadow @Final private List<ServerPlayerEntity> players;

    @Shadow public abstract List<Entity> getEntities(EntityType<?> entityType, Predicate<? super Entity> predicate);

    @Shadow @Final private Map<UUID, Entity> entitiesByUuid;

    @Override
    public int cachedChunks() {
        return this.getChunkManager().getLoadedChunkCount();
    }

    @Override
    public int totalLoadedChunks() {
        return this.getChunkManager().getTotalChunksLoadedCount();
    }

    @Override
    public int loadedEntities() {
        return this.entitiesByUuid.size();
    }

    @Override
    public int players() {
        return this.players.size();
    }

}
