package org.kilocraft.essentials.mixin.patch.technical;

import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.world.SpawnDensityCapper;
import org.kilocraft.essentials.mixin.accessor.ThreadedAnvilChunkStorageAccessor;
import org.kilocraft.essentials.patch.SpawnUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SpawnDensityCapper.class)
public abstract class SpawnDensityCapperMixin {

    @Shadow
    @Final
    private ThreadedAnvilChunkStorage threadedAnvilChunkStorage;

    @Redirect(
            method = "canSpawn",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/SpawnDensityCapper$DensityCap;canSpawn(Lnet/minecraft/entity/SpawnGroup;)Z"
            )
    )
    public boolean customSpawnGroupCap(SpawnDensityCapper.DensityCap densityCap, SpawnGroup spawnGroup) {
        return densityCap.spawnGroupsToDensity.getOrDefault(spawnGroup, 0) < SpawnUtil.getPersonalMobCap(((ThreadedAnvilChunkStorageAccessor) this.threadedAnvilChunkStorage).getWorld(), spawnGroup);
    }

}
