package org.kilocraft.essentials.mixin;

import net.minecraft.SharedConstants;
import net.minecraft.server.rcon.RconClient;
import org.kilocraft.essentials.api.KiloEssentials;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.BufferedInputStream;
import java.net.Socket;

@Mixin(RconClient.class)
public class RconClientMixin {

    @Shadow
    @Final
    private String password;

    @Shadow @Final private Socket socket;

    @Inject(method = "run", at = @At(value = "INVOKE", target = "Ljava/lang/String;equals(Ljava/lang/Object;)Z"), locals = LocalCapture.CAPTURE_FAILHARD)
    public void onPasswordCheck(CallbackInfo ci, BufferedInputStream inputStream, int var_1, int var_2, int var_3, String string) {
        if (!string.equals(this.password) && SharedConstants.isDevelopment) {
            KiloEssentials.getLogger().info("RCON: " + this.socket.getInetAddress().toString() + " attempted to connect with " + this.password);
        }
    }

}
