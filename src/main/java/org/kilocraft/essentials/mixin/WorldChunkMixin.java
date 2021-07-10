package org.kilocraft.essentials.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.WorldChunk;
import org.kilocraft.essentials.patch.perPlayerMobSpawn.PooledHashSets;
import org.kilocraft.essentials.patch.perPlayerMobSpawn.ThreadedAnvilChunkStorageInterface;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldChunk.class)
public class WorldChunkMixin {

    @Shadow
    @Final
    private ChunkPos pos;

    @Shadow
    @Final
    private World world;

    @Inject(method = "canTickBlockEntity", at = @At(value = "HEAD"), cancellable = true)
    public void shouldTickBlockEntity(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        ChunkManager chunkManager = world.getChunkManager();
        if (chunkManager instanceof ServerChunkManager serverChunkManager) {
            PooledHashSets.PooledObjectLinkedOpenHashSet<ServerPlayerEntity> range = ((ThreadedAnvilChunkStorageInterface) serverChunkManager.threadedAnvilChunkStorage).getMobDistanceMap().getPlayersInRange(this.pos);
            if (range.size() == 0) cir.setReturnValue(false);
        }
    }

}
