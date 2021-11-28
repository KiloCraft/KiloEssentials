package org.kilocraft.essentials.patch;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import org.kilocraft.essentials.mixin.accessor.ServerChunkCacheAccessor;

public final class ChunkManager {

    private ChunkManager() {
    }

    /**
     * Returns the BlockState at {@param pos} in {@param world} if the position is loaded.
     */

    public static BlockState getStateIfLoaded(Level world, BlockPos pos) {
        final LevelChunk chunk = getChunkIfLoaded(world, pos);
        return chunk != null ? chunk.getBlockState(pos) : Blocks.VOID_AIR.defaultBlockState();
    }

    /**
     * Returns the chunk at {@param pos} in {@param world} if the position is loaded.
     */

    public static LevelChunk getChunkIfLoaded(Level world, BlockPos pos) {
        final ChunkHolder holder = getChunkHolder(world, pos);
        return holder != null ? holder.getFullChunkFuture().getNow(ChunkHolder.UNLOADED_LEVEL_CHUNK).left().orElse(null) : null;
    }

    /**
     * Returns the chunk at {@param pos} in {@param world} if the location is visible.
     */

    public static BlockState getStateIfVisible(LevelReader world, BlockPos pos) {
        final ChunkAccess chunk = getChunkIfVisible(world, pos);
        return chunk != null ? chunk.getBlockState(pos) : Blocks.VOID_AIR.defaultBlockState();
    }

    public static ChunkAccess getChunkIfVisible(LevelReader world, BlockPos pos) {
        final ChunkHolder holder = getChunkHolder(world, pos);
        return holder != null ? holder.getFullChunkFuture().getNow(ChunkHolder.UNLOADED_LEVEL_CHUNK).left().orElse(null) : null;
    }

    /**
     * Returns a boolean that decides whether or not the chunk at {@param pos} in {@param world} is visible.
     */

    public static boolean isChunkVisible(LevelReader world, BlockPos pos) {
        return world instanceof ServerLevel serverWorld && isChunkVisible(serverWorld.getChunkSource(), pos.getX() >> 4, pos.getZ() >> 4);
    }

    public static boolean isChunkVisible(ServerChunkCache manager, int x, int z) {
        return manager.isPositionTicking(ChunkPos.asLong(x, z));
    }

    public static boolean isChunkLoaded(Level world, BlockPos pos) {
        return isChunkLoaded(getChunkHolder(world, pos));
    }

    public static boolean isChunkLoaded(ChunkHolder holder) {
        return holder != null && holder.wasAccessibleSinceLastSave();
    }

    /**
     * Returns the ChunkHolder at {@param pos} in {@param world} if the location is loaded.
     */

    public static ChunkHolder getChunkHolder(LevelReader world, BlockPos pos) {
        return world instanceof ServerLevel serverWorld ? getChunkHolder(serverWorld.getChunkSource(), pos.getX() >> 4, pos.getZ() >> 4) : null;
    }

    public static ChunkHolder getChunkHolder(ServerChunkCache manager, int x, int z) {
        return ((ServerChunkCacheAccessor) manager).getVisibleChunkIfPresent(ChunkPos.asLong(x, z));
    }

    /**
     * Returns the ChunkTicketManager from {@param world}.
     */

    public static DistanceManager getTicketManager(LevelReader world) {
        return world instanceof ServerLevel serverWorld ? getTicketManager(serverWorld.getChunkSource()) : null;
    }

    public static DistanceManager getTicketManager(ServerChunkCache manager) {
        return ((ServerChunkCacheAccessor) manager).getDistanceManager();
    }
}
