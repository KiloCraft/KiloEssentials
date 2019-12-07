package org.kilocraft.essentials.mixin.events;

import net.minecraft.server.MinecraftServer;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.events.server.ServerSaveEventImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer$OnSave {

    @Inject(method = "save", at = @At(value = "HEAD", target = "Lnet/minecraft/server/MinecraftServer;save(ZZZ)Z"))

    private void modify(boolean bl, boolean bl2, boolean bl3, CallbackInfoReturnable<Boolean> cir) {
        KiloServer.getServer().triggerEvent(new ServerSaveEventImpl(KiloServer.getServer().getVanillaServer()));
    }

}
