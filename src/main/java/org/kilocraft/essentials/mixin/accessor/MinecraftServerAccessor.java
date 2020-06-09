package org.kilocraft.essentials.mixin.accessor;

import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecraftServer.class)
public interface MinecraftServerAccessor {
    @Accessor("serverThread")
    Thread getServerThread();

    @Accessor("LOGGER")
    Logger getLogger();
}
