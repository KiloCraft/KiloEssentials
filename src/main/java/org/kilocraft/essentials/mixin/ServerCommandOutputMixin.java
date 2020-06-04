package org.kilocraft.essentials.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.ServerCommandOutput;
import org.kilocraft.essentials.KiloDebugUtils;
import org.kilocraft.essentials.KiloEssentialsImpl;
import org.kilocraft.essentials.KiloEvents;
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
        String brand = String.format(
                ModConstants.getProperties().getProperty("server.brand.full"),
                ModConstants.getMinecraftVersion(),
                ModConstants.getLoaderVersion(),
                ModConstants.getMappingsVersion(),
                ModConstants.getVersion()
        );

        KiloServer.setServer(
                new ServerImpl(
                    minecraftServer,
                    new EventRegistryImpl(),
                    new ServerUserManager(minecraftServer.getPlayerManager()),
                    brand
                )
        );

        KiloServer.getLogger().info("Server set: " + brand);

        KiloDebugUtils.validateDebugMode(false);
        new KiloEssentialsImpl(new KiloEvents());
    }
}
