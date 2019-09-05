package org.kilocraft.essentials.api.world;

import org.kilocraft.essentials.api.math.Vec3d;

public interface Block {

    /**
     * Gets the block's coordinates
     *
     * @return the block's coordinates
     */
    Vec3d getLocation();

    /**
     * Gets the block's world
     *
     * @return the block's world
     */
    World getWorld();

}
