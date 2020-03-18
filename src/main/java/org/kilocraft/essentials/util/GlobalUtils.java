package org.kilocraft.essentials.util;

import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.bungee.KiloEssentialsBungee;
import org.kilocraft.essentials.bungee.KiloEssentialsBungeePlugin;
import org.kilocraft.essentials.bungee.api.ProxiedPlayerUtil;
import org.kilocraft.essentials.config.KiloConfig;

import java.net.SocketAddress;
import java.util.UUID;

public class GlobalUtils {
    private static boolean isProxy = KiloConfig.main().server().proxyMode && KiloEssentialsBungeePlugin.isEnabled();

    @Nullable
    public static SocketAddress getSocketAddress(UUID uuid) {
        if (isProxy) {
            return ProxiedPlayerUtil.getRealSocketAddress(uuid);
        }

        OnlineUser user = KiloServer.getServer().getOnlineUser(uuid);
        return user != null && user.getConnection() != null ? user.getConnection().getAddress() : null;
    }

}
