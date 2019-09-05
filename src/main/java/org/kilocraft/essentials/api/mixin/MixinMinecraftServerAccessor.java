package org.kilocraft.essentials.api.mixin;

import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.Logger;
import org.kilocraft.essentials.api.util.MinecraftServerLoggable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServerAccessor implements MinecraftServerLoggable {

    @Final
    @Shadow
    private static Logger LOGGER;

    public Logger getLogger() {
        return LOGGER;
    }
}
