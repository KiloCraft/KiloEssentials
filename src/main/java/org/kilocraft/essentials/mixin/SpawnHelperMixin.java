package org.kilocraft.essentials.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.chunk.WorldChunk;
import org.kilocraft.essentials.util.math.DataTracker;
import org.kilocraft.essentials.util.settings.ServerSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpawnHelper.class)
public abstract class SpawnHelperMixin {

    @Inject(method = "spawnEntitiesInChunk(Lnet/minecraft/entity/SpawnGroup;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/chunk/WorldChunk;Lnet/minecraft/world/SpawnHelper$Checker;Lnet/minecraft/world/SpawnHelper$Runner;)V", at = @At(value = "HEAD"), cancellable = true)
    private static void ke$adjustMobcap(SpawnGroup spawnGroup, ServerWorld world, WorldChunk chunk, SpawnHelper.Checker checker, SpawnHelper.Runner runner, CallbackInfo ci) {
        int tickDistance = ServerSettings.getInt("tick.distance");
        if (tickDistance != -1) {
            Entity player = world.getClosestPlayer(chunk.getPos().getStartX() + 8, 128, chunk.getPos().getStartZ() + 8, -1.0D, false);
            if (player != null) {
                if (chunk.getPos().getChebyshevDistance(player.getChunkPos()) > tickDistance) {
                    DataTracker.cSpawnAttempts.track();
                    ci.cancel();
                    return;
                }
            }
        }
        DataTracker.spawnAttempts.track();
        if (!ServerSettings.getBoolean("patch.ppmobcap")) return;
        PlayerEntity player = world.getClosestPlayer(chunk.getPos().getStartX() + 8, 128, chunk.getPos().getStartZ() + 8, -1.0D, false);
        if (player == null) return;
        int i = DataTracker.cachedEntityCount.get(world).get(player).getInt(spawnGroup);
/*
        int i = 0;
        for (Entity entity : world.iterateEntities()) {
            if (entity.getType().getSpawnGroup().equals(spawnGroup)) {
                if (player.getChunkPos().getChebyshevDistance(entity.getChunkPos()) <= ServerSettings.getInt("view_distance") && entity.getEntityWorld().equals(player.getEntityWorld())) {
                    i++;
                }
            }
        }*/
        if (i > spawnGroup.getCapacity() * ServerSettings.getFloat("patch.ppmobcap.max")) {
            ci.cancel();
        }
    }

}
