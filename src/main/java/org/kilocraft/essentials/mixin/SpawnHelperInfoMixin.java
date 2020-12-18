package org.kilocraft.essentials.mixin;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.world.SpawnHelper;
import org.kilocraft.essentials.commands.misc.MobCapCommand;
import org.kilocraft.essentials.mixin.accessor.SpawnHelperAccessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SpawnHelper.Info.class)
public class SpawnHelperInfoMixin {

    @Shadow @Final private int spawningChunkCount;

    @Shadow @Final private Object2IntOpenHashMap<SpawnGroup> groupToCount;

    @Inject(method = "isBelowCap", at = @At("HEAD"), cancellable = true)
    private void changeMobCaps(SpawnGroup spawnGroup, CallbackInfoReturnable<Boolean> cir) {
        int i = (int) ((spawnGroup.getCapacity() * this.spawningChunkCount / SpawnHelperAccessor.getChunkArea()) * MobCapCommand.mult);
        cir.setReturnValue(this.groupToCount.getInt(spawnGroup) < i);
    }

}
