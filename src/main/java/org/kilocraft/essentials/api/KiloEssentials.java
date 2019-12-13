/*
 * MIT License
 *
 * Copyright (c) 2019 KiloCraft
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.kilocraft.essentials.api;

import net.minecraft.server.command.ServerCommandSource;
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

    static void registerPermission(String node) {
        KiloEssentialsImpl.registerPermission(node);
    }

    static String getPermissionFor(String node) {
        return KiloEssentialsImpl.getPermissionFor(node);
    }

    static Server getServer() {
        return KiloServer.getServer();
    }

    static boolean hasPermissionNode(ServerCommandSource source, String fullNode) {
        return KiloEssentialsImpl.hasPermissionNode(source, fullNode);
    }

    static boolean hasPermissionNode(ServerCommandSource source, String fullNode, int opLevel) {
        return KiloEssentialsImpl.hasPermissionNode(source, fullNode, opLevel);
    }

    MessageUtil getMessageUtil();

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
