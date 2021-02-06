package org.kilocraft.essentials.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.chunk.Chunk;
import org.kilocraft.essentials.util.DataTracker;
import org.kilocraft.essentials.util.settings.ServerSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SpawnHelper.class)
public abstract class SpawnHelperMixin {

    @Inject(method = "isAcceptableSpawnPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/Chunk;getPos()Lnet/minecraft/util/math/ChunkPos;"), cancellable = true)
    private static void shouldSpawnEntities(ServerWorld serverWorld, Chunk chunk, BlockPos.Mutable mutable, double d, CallbackInfoReturnable<Boolean> cir) {
        int tickDistance = ServerSettings.TICK_DISTANCE.getValue();
        if (tickDistance != -1) {
            Entity player = serverWorld.getClosestPlayer(chunk.getPos().getStartX() + 8, 128, chunk.getPos().getStartZ() + 8, -1.0D, false);
            if (player != null) {
                if (chunk.getPos().method_24022(player.getChunkPos()) > tickDistance) {
                    DataTracker.add(DataTracker.cSpawnAttempts);
                    cir.setReturnValue(false);
                    return;
                }
            }
        }
        DataTracker.add(DataTracker.spawnAttempts);
    }
}
