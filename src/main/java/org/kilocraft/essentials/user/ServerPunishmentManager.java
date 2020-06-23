package org.kilocraft.essentials.user;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.user.PunishmentManager;
import org.kilocraft.essentials.api.util.EntityIdentifiable;
import org.kilocraft.essentials.util.MutedPlayerList;

import java.util.Date;
import java.util.Objects;

public class ServerPunishmentManager implements PunishmentManager {

    public ServerPunishmentManager() {
    }

    @Override
    public boolean isMuted(EntityIdentifiable user) {
        MutedPlayerList mutedPlayerList = KiloServer.getServer().getUserManager().getMutedPlayerList();
        MinecraftServer server = KiloServer.getServer().getMinecraftServer();
        GameProfile victim = server.getUserCache().getByUuid(user.getId());
        if (victim == null) {
            return false;
        }

        if (mutedPlayerList.contains(victim) && mutedPlayerList.get(victim) != null) {
            Date expiry = Objects.requireNonNull(mutedPlayerList.get(victim)).getExpiryDate();
            if (expiry != null) {
                return new Date().before(expiry);
            }
        }
        return false;
    }
}
