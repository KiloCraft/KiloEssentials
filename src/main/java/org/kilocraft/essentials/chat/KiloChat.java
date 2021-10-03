package org.kilocraft.essentials.chat;

import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.text.ComponentText;

public class KiloChat {

    public static void broadCastToConsole(String message) {
        for (String s : message.split("\n")) {
            KiloEssentials.getLogger().info(ComponentText.clearFormatting(s));
        }
    }

    public static void broadCast(String message) {
        for (ServerPlayerEntity player : KiloEssentials.getMinecraftServer().getPlayerManager().getPlayerList()) {
            KiloEssentials.getUserManager().getOnline(player).sendMessage(message);
        }
    }
}