package org.kilocraft.essentials.user;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.user.PunishmentManager;
import org.kilocraft.essentials.api.util.EntityIdentifiable;
import org.kilocraft.essentials.util.MutedPlayerList;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

public class ServerPunishmentManager implements PunishmentManager {

    public ServerPunishmentManager() {
    }

    @Override
    public boolean isMuted(EntityIdentifiable user) {
        MutedPlayerList mutedPlayerList = KiloEssentials.getUserManager().getMutedPlayerList();
        MinecraftServer server = KiloEssentials.getMinecraftServer();
        Optional<GameProfile> profile = server.getUserCache().getByUuid(user.getId());
        if (profile.isEmpty()) {
            return false;
        }

        if (mutedPlayerList.contains(profile.get()) && mutedPlayerList.get(profile.get()) != null) {
            Date expiry = Objects.requireNonNull(mutedPlayerList.get(profile.get())).getExpiryDate();
            if (expiry != null) {
                return new Date().before(expiry);
            }
        }
        return false;
    }
}
