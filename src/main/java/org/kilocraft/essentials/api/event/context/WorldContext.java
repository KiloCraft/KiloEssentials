package org.kilocraft.essentials.api.event.context;

import net.minecraft.server.world.ServerWorld;

/**
 * Represents a context in which a world is involved.
 */
public interface WorldContext extends Contextual {
    ServerWorld getWorld();
}
