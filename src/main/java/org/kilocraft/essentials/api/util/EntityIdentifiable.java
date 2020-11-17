package org.kilocraft.essentials.api.util;

import com.mojang.authlib.GameProfile;

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

    static EntityIdentifiable fromGameProfile(final GameProfile profile) {
        return new EntityIdentifiable() {
            @Override
            public UUID getId() {
                return profile.getId();
            }

            @Override
            public String getName() {
                return profile.getName();
            }
        };
    }
}
