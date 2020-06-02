package org.kilocraft.essentials.api.util;

import java.util.UUID;

/**
 * A simple interface for representing an Identifiable Entity
 */
public interface EntityIdentifiable {
    /**
     * Gets the Universally Unique Identifier of this Entity
     * @return UUID of this Entity
     */
    UUID getId();

    /**
     * Gets the name of this Entity
     * @return name as String
     */
    String getName();
}
