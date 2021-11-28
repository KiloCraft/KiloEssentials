package org.kilocraft.essentials.mixin.accessor;

import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.server.level.ServerChunkCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ServerChunkCache.class)
public interface ServerChunkCacheAccessor {

    @Accessor("distanceManager")
    DistanceManager getDistanceManager();

    @Invoker("getVisibleChunkIfPresent")
    ChunkHolder getVisibleChunkIfPresent(long pos);

}
