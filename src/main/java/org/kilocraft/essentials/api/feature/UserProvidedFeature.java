package org.kilocraft.essentials.api.feature;

import org.kilocraft.essentials.api.config.configurable.ConfigurableFeature;
import org.kilocraft.essentials.user.ServerUser;

import java.util.function.Function;

public interface UserProvidedFeature extends ConfigurableFeature {
    /**
     * Returns true if this is a proxied key accessor.
     */
    boolean isProxy();

    /**
     * Creates an instance of the ConfigurableFeature
     * @param <F> The type of the ConfigurableFeature
     * @return A new instance of the configurable feature.
     */
    <F extends ConfigurableFeature> Function<ServerUser, F> provider();
}
