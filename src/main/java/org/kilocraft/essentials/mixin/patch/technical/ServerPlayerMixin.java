package org.kilocraft.essentials.mixin.patch.technical;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.kilocraft.essentials.user.ServerUser;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {

    @Inject(
            method = "teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDFF)V",
            at = @At("RETURN")
    )
    private void savePlayerLocation(ServerLevel serverWorld, double d, double e, double f, float g, float h, CallbackInfo ci) {
        // Save user location for /back
        ServerUser.saveLocationOf((ServerPlayer) (Object) this);
    }

}
