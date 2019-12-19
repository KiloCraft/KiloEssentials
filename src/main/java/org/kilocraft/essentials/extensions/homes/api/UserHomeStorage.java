package org.kilocraft.essentials.extensions.homes.api;

import org.kilocraft.essentials.api.feature.ConfigurableFeature;
import org.kilocraft.essentials.api.feature.UserProvidedFeature;
import org.kilocraft.essentials.user.ServerUser;

import java.util.function.Function;

public class UserHomeStorage implements UserProvidedFeature {
    @Override
    public boolean isProxy() {
        return false;
    }

    @Override
    public Function<ServerUser, UserHomeStorage> provider() {
        return null; // TODO impl
    }

    @Override
    public boolean register() {
        return false;
    }
}
