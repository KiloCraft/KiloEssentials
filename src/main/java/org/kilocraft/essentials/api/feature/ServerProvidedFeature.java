package org.kilocraft.essentials.api.feature;

import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.server.Server;
import org.kilocraft.essentials.user.ServerUser;

import java.util.function.Function;

public interface ServerProvidedFeature extends ConfigurableFeature {
    boolean isProxy();

    /**
     * Creates an instance of the ConfigurableFeature
     * @param <F> The type of the ConfigurableFeature
     * @return A new instance of the configurable feature.
     */
    <F extends ConfigurableFeature> Function<Server, F> provider();
}
