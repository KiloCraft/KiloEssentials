package org.kilocraft.essentials.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.ServerCommandOutput;
import org.kilocraft.essentials.ServerImpl;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.events.EventRegistryImpl;
import org.kilocraft.essentials.user.ServerUserManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerCommandOutput.class)
public abstract class ServerCommandOutputMixin {

    @Inject(at = @At("RETURN"), method = "<init>")
    private void kilo$init(MinecraftServer minecraftServer, CallbackInfo ci) {

        new ModConstants().loadConstants();

        KiloServer.setServer(
                new ServerImpl(
                    minecraftServer,
                    new EventRegistryImpl(),
                    new ServerUserManager(minecraftServer.getPlayerManager()),
                    String.format(
                            ModConstants.getProperties().getProperty("server.brand"),
                                ModConstants.getVersion(),
                                ModConstants.getLoaderVersion(),
                                ModConstants.getMappingsVersion())
            )
        );

        ModConstants.getLogger().info("Server set: " + KiloServer.getServer().getBrandName());
    }
}
