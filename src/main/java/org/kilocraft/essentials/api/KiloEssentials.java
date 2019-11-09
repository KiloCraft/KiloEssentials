package org.kilocraft.essentials.api;

import org.apache.logging.log4j.Logger;
import org.kilocraft.essentials.KiloEssentialsImpl;
import org.kilocraft.essentials.api.feature.ConfigurableFeature;
import org.kilocraft.essentials.api.feature.FeatureNotPresentException;
import org.kilocraft.essentials.api.feature.FeatureType;
import org.kilocraft.essentials.api.server.Server;

public interface KiloEssentials {
    static KiloEssentials getInstance() {
        return KiloEssentialsImpl.getInstance();
    }

    static Logger getLogger() {
        return KiloEssentialsImpl.getLogger();
    }

    static String getPermissionFor(String node) {
        return KiloEssentialsImpl.getPermissionFor(node);
    }

    Server getServer();

    <F extends ConfigurableFeature> FeatureType<F> registerFeature(FeatureType<F> featureType);

    <F extends ConfigurableFeature> F getFeature(FeatureType<F> type) throws FeatureNotPresentException;
}
