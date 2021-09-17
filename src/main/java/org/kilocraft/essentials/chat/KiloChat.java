package org.kilocraft.essentials.chat;

import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.config.ConfigVariableFactory;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.config.messages.Messages;
import org.kilocraft.essentials.user.OnlineServerUser;

public class KiloChat {
    private static final Messages messages = KiloConfig.messages();

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

    public static void onUserJoin(OnlineServerUser user) {
        broadCast(ConfigVariableFactory.replaceUserVariables(messages.events().userJoin, user));
    }

    public static void onUserLeave(OnlineServerUser user) {
        broadCast(ConfigVariableFactory.replaceUserVariables(messages.events().userLeave, user));
    }
}