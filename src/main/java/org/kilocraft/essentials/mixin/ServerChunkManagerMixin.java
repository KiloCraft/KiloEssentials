package org.kilocraft.essentials.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import org.kilocraft.essentials.util.DataTracker;
import org.kilocraft.essentials.util.settings.ServerSettings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerChunkManager.class)
public class ServerChunkManagerMixin {

    @Shadow
    @Final
    private ServerWorld world;

    @Inject(method = "shouldTickChunk", at = @At(value = "HEAD"), cancellable = true)
    public void shouldSpawnEntities(ChunkPos chunkPos, CallbackInfoReturnable<Boolean> cir) {
        int tickDistance = ServerSettings.TICK_DISTANCE.getValue();
        if (tickDistance != -1) {
            Entity player = this.world.getClosestPlayer(chunkPos.getStartX() + 8, 128, chunkPos.getStartZ() + 8, -1.0D, false);
            if (player != null) {
                if (chunkPos.method_24022(player.getChunkPos()) > tickDistance) {
                    DataTracker.add(DataTracker.cSpawnAttempts);
                    cir.setReturnValue(false);
                    return;
                }
            }
        }
        DataTracker.add(DataTracker.spawnAttempts);
    }

}
