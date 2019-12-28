package org.kilocraft.essentials.api.event.context;

import net.minecraft.util.math.Vec3d;

/**
 * Represents a Context which involves a location.
 */
public interface LocationContext extends Contextual {

    /**
     * Gets the location of this event
     *
     * @return A Vec3d of the location
     */
    Vec3d getLocation();
}
