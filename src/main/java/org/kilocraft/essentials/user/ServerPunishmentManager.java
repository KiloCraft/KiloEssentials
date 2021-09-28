package org.kilocraft.essentials.user;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.user.PunishmentManager;
import org.kilocraft.essentials.util.MutedPlayerEntry;
import org.kilocraft.essentials.util.MutedPlayerList;

import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public class ServerPunishmentManager implements PunishmentManager {

    @Override
    public boolean isMuted(UUID uuid) {
        MutedPlayerList mutedPlayerList = KiloEssentials.getUserManager().getMutedPlayerList();
        MinecraftServer server = KiloEssentials.getMinecraftServer();
        Optional<GameProfile> optional = server.getUserCache().getByUuid(uuid);
        if (optional.isEmpty()) {
            return false;
        }

        final MutedPlayerEntry mutedPlayerEntry = mutedPlayerList.get(optional.get());
        if (mutedPlayerEntry != null) {
            Date expiry = mutedPlayerEntry.getExpiryDate();
            if (expiry != null) {
                return new Date().before(expiry);
            } else {
                return true;
            }
        }
        return false;
    }
}
