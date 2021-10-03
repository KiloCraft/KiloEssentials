package org.kilocraft.essentials.mixin.patch.performance;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.PlayerChunkWatchingManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ThreadedAnvilChunkStorage.class)
public interface ThreadedAnvilChunkStorageAccessor {

    @Accessor("chunkHolders")
    Long2ObjectLinkedOpenHashMap<ChunkHolder> getChunkHolders();

    @Accessor("playerChunkWatchingManager")
    PlayerChunkWatchingManager getPlayerChunkWatchingManager();

    @Accessor("world")
    ServerWorld getWorld();

    @Invoker("isTooFarFromPlayersToSpawnMobs")
    boolean tooFarFromPlayersToSpawnMobs(ChunkPos chunkPos);
}
