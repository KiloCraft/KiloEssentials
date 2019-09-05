package org.kilocraft.essentials.api.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.ServerCommandOutput;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.Mod;
import org.kilocraft.essentials.api.event.eventImpl.EventRegistryImpl;
import org.kilocraft.essentials.api.server.ServerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerCommandOutput.class)
public class MixinMinecraftServerCommandOutput {
    @Inject(at = @At("RETURN"), method = "<init>")
    private void oky$init(MinecraftServer minecraftServer, CallbackInfo ci) {
        KiloServer.setServer(new ServerImpl(
                minecraftServer,
                new EventRegistryImpl(),
                String.format(Mod.properties.getProperty("server.brand"), Mod.getVersion(), Mod.getMinecraftVersion(), Mod.getLoaderVersion(), Mod.getMappingsVersion())
        ));
    }
}
