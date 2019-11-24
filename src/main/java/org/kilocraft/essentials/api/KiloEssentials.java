package org.kilocraft.essentials.api;

import org.apache.logging.log4j.Logger;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.KiloEssentialsImpl;
import org.kilocraft.essentials.api.feature.ConfigurableFeature;
import org.kilocraft.essentials.api.feature.FeatureNotPresentException;
import org.kilocraft.essentials.api.feature.FeatureType;
import org.kilocraft.essentials.api.feature.SingleInstanceConfigurableFeature;
import org.kilocraft.essentials.api.server.Server;
import org.kilocraft.essentials.api.user.User;
import org.kilocraft.essentials.util.messages.MessageUtil;

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

    MessageUtil getMessageUtil();

    Server getServer();

    ModConstants getConstants();

    KiloCommands getCommandHandler();

    <F extends ConfigurableFeature> FeatureType<F> registerFeature(FeatureType<F> featureType);

    /**
     * Gets a SingleInstanceConfigurableFeature from the instance of the mod.
     * <p> NOTE: If you are looking for where Homes and Particles are handled, see {@link User#feature(FeatureType)}
     * @param type The FeatureType of the feature.
     * @param <F> The generic type of the feature being obtained.
     * @return The instance of the feature.
     * @throws FeatureNotPresentException If the feature type is disabled, not present or not a SingleInstanceConfigurableFeature.
     */
    <F extends SingleInstanceConfigurableFeature> F getFeature(FeatureType<F> type) throws FeatureNotPresentException;
}
