package org.kilocraft.essentials.api.text;

import java.util.UUID;

/**
 * @deprecated Use {@link org.kilocraft.essentials.api.util.EntityIdentifiable} instead
 */

@Deprecated
public interface MessageReceptionist {
    String getName();

    UUID getId();
}
