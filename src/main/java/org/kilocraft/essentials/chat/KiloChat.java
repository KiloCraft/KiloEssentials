package org.kilocraft.essentials.chat;

import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.config.ConfigVariableFactory;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.config.messages.Messages;
import org.kilocraft.essentials.user.ServerUser;

import static org.kilocraft.essentials.api.KiloServer.getServer;

public class KiloChat {
	private static final Messages messages = KiloConfig.messages();

	public static String getFormattedLang(String key) {
		return getFormattedString(ModConstants.getStrings().getProperty(key));
	}

	public static String getFormattedLang(String key, Object... objects) {
		return getFormattedString(ModConstants.getStrings().getProperty(key), objects);
	}

	private static String getFormattedString(String string, Object... objects) {
		return (objects.length > 0) ? String.format(string, objects) : string;
	}

	public static void broadCastToConsole(String message) {
		getServer().sendMessage(message);
	}

	public static void broadCast(String message) {
        for (ServerPlayerEntity player : getServer().getPlayerManager().getPlayerList()) {
            getServer().getOnlineUser(player).sendMessage(message);
        }
    }

	public static void onUserJoin(ServerUser user) {
		broadCast(ConfigVariableFactory.replaceUserVariables(messages.events().userJoin, user));
	}

    public static void onUserLeave(ServerUser user) {
        broadCast(ConfigVariableFactory.replaceUserVariables(messages.events().userLeave, user));
    }
}