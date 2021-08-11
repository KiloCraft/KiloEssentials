package org.kilocraft.essentials.mixin.patch;

import net.minecraft.entity.mob.ShulkerEntity;
import org.kilocraft.essentials.util.settings.ServerSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(ShulkerEntity.class)
public abstract class ShulkerEntityMixinPatch {

    @Inject(method = "spawnNewShulker", at = @At(value = "HEAD"), cancellable = true)
    public void onShulkerSpawnAttempt(CallbackInfo ci) {
        if (ServerSettings.getDouble("patch.shulker_spawn_chance") >= new Random().nextDouble() * 100) {
            ci.cancel();
        }
    }

}
