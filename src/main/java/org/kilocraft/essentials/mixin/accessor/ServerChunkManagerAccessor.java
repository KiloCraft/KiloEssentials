package org.kilocraft.essentials.mixin.accessor;

import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.server.world.ServerChunkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ServerChunkManager.class)
public interface ServerChunkManagerAccessor {

    /*
     * Copied from:
     * https://github.com/Wesley1808/ServerCore-Fabric/blob/1.17.1/src/main/java/org/provim/servercore/mixin/accessor/ServerChunkManagerAccessor.java
     * */

    @Accessor("ticketManager")
    ChunkTicketManager getTicketManager();

    @Invoker("getChunkHolder")
    ChunkHolder getHolder(long pos);
}
