package org.kilocraft.essentials.api.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.ServerCommandOutput;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.ModData;
import org.kilocraft.essentials.api.event.eventImpl.EventRegistryImpl;
import org.kilocraft.essentials.api.server.ServerImpl;
import org.kilocraft.essentials.user.UserManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerCommandOutput.class)
public abstract class MixinServerCommandOutput {
    @Shadow @Final private MinecraftServer server;

    @Inject(at = @At("RETURN"), method = "<init>")
    private void kilo$init(MinecraftServer minecraftServer, CallbackInfo ci) {

        new ModData();

        KiloServer.setServer(
                new ServerImpl(
                    minecraftServer,
                    new EventRegistryImpl(),
                    new UserManager(),
                    String.format(
                            ModData.getProperties().getProperty("server.brand"),
                                ModData.getVersion(),
                                ModData.getLoaderVersion(),
                                ModData.getMappingsVersion())
            )
        );

        ModData.getLogger().info("Server set: " + KiloServer.getServer().getBrandName());
    }
}
