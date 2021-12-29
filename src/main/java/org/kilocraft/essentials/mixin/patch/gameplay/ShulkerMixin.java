package org.kilocraft.essentials.mixin.patch.gameplay;

import org.kilocraft.essentials.util.settings.ServerSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

import net.minecraft.world.entity.monster.Shulker;

@Mixin(Shulker.class)
public abstract class ShulkerMixin {

    // Configurable shulker spawn chance
    @Inject(
            method = "hitByShulkerBullet",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    public void onShulkerSpawnAttempt(CallbackInfo ci) {
        if (ServerSettings.getDouble("patch.shulker_spawn_chance") >= new Random().nextDouble() * 100) {
            ci.cancel();
        }
    }

}
