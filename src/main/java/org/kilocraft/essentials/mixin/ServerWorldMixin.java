package org.kilocraft.essentials.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.dimension.DimensionType;
import org.kilocraft.essentials.util.math.DataTracker;
import org.kilocraft.essentials.util.settings.ServerSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World {

    protected ServerWorldMixin(MutableWorldProperties mutableWorldProperties, RegistryKey<World> registryKey, DimensionType dimensionType, Supplier<Profiler> supplier, boolean bl, boolean bl2, long l) {
        super(mutableWorldProperties, registryKey, dimensionType, supplier, bl, bl2, l);
    }

    @Inject(method = "tickChunk", at = @At(value = "HEAD"), cancellable = true)
    public void shouldTickChunk(WorldChunk worldChunk, int i, CallbackInfo ci) {
        ChunkPos chunkPos = worldChunk.getPos();
        int tickDistance = ServerSettings.tickDistance;
        if (tickDistance != -1) {
            Entity player = this.getClosestPlayer(chunkPos.getStartX() + 8, 128, chunkPos.getStartZ() + 8, -1.0D, false);
            if (player != null) {
                if (chunkPos.getChebyshevDistance(player.getChunkPos()) > tickDistance) {
                    DataTracker.cTickedChunks.track();
                    ci.cancel();
                    return;
                }
            }
        }
        DataTracker.tickedChunks.track();
    }

}
