package org.kilocraft.essentials.craft.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerMetadata;
import net.minecraft.server.ServerNetworkIo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer$run {
    @Shadow @Final private ServerMetadata metadata;

    @Shadow private String serverIp;

    @Shadow @Final private ServerNetworkIo networkIo;

    @Inject(at = @At(value = "HEAD", target = "Lnet/minecraft/server/MinecraftServer;run()V", ordinal = 1), method = "run")
    public void mixin$setupServer(CallbackInfo ci) {
    }

//    @Redirect(
//            method = "run",
//            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/ServerMetadata;setDescription(Lnet/minecraft/text/Text;)V")
//    )
//
//    private void modify(ServerMetadata serverMetadata, Text text_1) {
//    }

}
