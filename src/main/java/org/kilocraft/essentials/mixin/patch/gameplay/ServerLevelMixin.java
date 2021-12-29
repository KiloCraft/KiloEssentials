package org.kilocraft.essentials.mixin.patch.gameplay;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import org.kilocraft.essentials.util.settings.ServerSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {

    @Shadow
    public abstract void playSound(Player playerEntity, double d, double e, double f, SoundEvent soundEvent, SoundSource soundCategory, float g, float h);

    // Change global to local sound events
    @Inject(
            method = "globalLevelEvent",
            at = @At("HEAD"),
            cancellable = true
    )
    public void shouldWeAnnoyEveryone(int i, BlockPos blockPos, int j, CallbackInfo ci) {
        if (!ServerSettings.getBoolean("patch.global_sound")) {
            ci.cancel();
            SoundEvent soundEvent = null;
            float g = 1.0F;
            float h = 1.0F;
            switch (i) {
                case 1023 -> soundEvent = SoundEvents.WITHER_SPAWN;
                case 1028 -> {
                    soundEvent = SoundEvents.ENDER_DRAGON_DEATH;
                    g = 5.0F;
                }
                case 1038 -> soundEvent = SoundEvents.END_PORTAL_SPAWN;
            }
            if (soundEvent != null)
                this.playSound(null, blockPos.getX(), blockPos.getY(), blockPos.getZ(), soundEvent, SoundSource.HOSTILE, g, h);
        }
    }

}
