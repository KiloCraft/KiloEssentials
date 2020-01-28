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

        String brand = ModConstants.getProperties().getProperty("server.brand");

        KiloServer.setServer(
                new ServerImpl(
                    minecraftServer,
                    new EventRegistryImpl(),
                    new ServerUserManager(minecraftServer.getPlayerManager()),
                    brand));

        ModConstants.getLogger().info("Server set: " + String.format(
                ModConstants.getProperties().getProperty("server.brand.full"),
                ModConstants.getVersion(),
                ModConstants.getLoaderVersion(),
                ModConstants.getMappingsVersion()));
    }
}
