package org.kilocraft.essentials.mixin.patch.technical;

import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.LocalMobCapCalculator;
import org.kilocraft.essentials.mixin.accessor.ChunkMapAccessor;
import org.kilocraft.essentials.patch.SpawnUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LocalMobCapCalculator.class)
public abstract class LocalMobCapCalculatorMixin {

    @Shadow @Final private ChunkMap chunkMap;

    @Redirect(
            method = "canSpawn",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/LocalMobCapCalculator$MobCounts;canSpawn(Lnet/minecraft/world/entity/MobCategory;)Z"
            )
    )
    public boolean customSpawnGroupCap(LocalMobCapCalculator.MobCounts densityCap, MobCategory spawnGroup) {
        return densityCap.counts.getOrDefault(spawnGroup, 0) < SpawnUtil.getPersonalMobCap(((ChunkMapAccessor) this.chunkMap).getLevel(), spawnGroup);
    }

}
