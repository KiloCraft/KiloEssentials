package org.kilocraft.essentials.mixin.patch;

import net.minecraft.entity.mob.ShulkerEntity;
import org.kilocraft.essentials.api.KiloEssentials;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(ShulkerEntity.class)
public class ShulkerEntityMixinPatch {

    @Inject(method = "spawnNewShulker", at = @At(value = "HEAD"), cancellable = true)
    public void onShulkerSpawnAttempt(CallbackInfo ci) {
        if (KiloEssentials.getInstance().getSettingManager().getShulkerSpawnChance() >= new Random().nextDouble() * 100) {
            ci.cancel();
        }
    }

}
