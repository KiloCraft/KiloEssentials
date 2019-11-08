package org.kilocraft.essentials;

import io.github.indicode.fabric.permissions.PermChangeBehavior;
import io.github.indicode.fabric.permissions.Thimble;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kilocraft.essentials.api.ModData;
import org.kilocraft.essentials.config.KiloConifg;
import org.kilocraft.essentials.api.config.configurable.ConfigurableFeatures;
import org.kilocraft.essentials.user.UserHomeHandler;
import org.kilocraft.essentials.misc.serverwarp.WarpManager;

import java.util.ArrayList;
import java.util.List;

public class KiloEssentials {
	private static Logger logger = LogManager.getFormatterLogger("KiloEssentials");
	private static List<String> initializedPerms = new ArrayList<>();

	public KiloEssentials(KiloEvents events, KiloCommands commands) {
		logger.info("Running KiloEssentials version " + ModData.getVersion());

		new KiloConifg();

		ConfigurableFeatures features = new ConfigurableFeatures();
		features.tryToRegister(new UserHomeHandler(), "PlayerHomes");
		features.tryToRegister(new WarpManager(), "ServerWideWarps");

		Thimble.permissionWriters.add((map, server) -> initializedPerms.forEach(perm -> map.registerPermission("kiloessentials." + perm, PermChangeBehavior.UPDATE_COMMAND_TREE)));

	}

	public static Logger getLogger() {
		return logger;
	}

	public static String getPermissionFor(String node) {
		if (!initializedPerms.contains(node))
			initializedPerms.add(node);
		return "kiloessentials." + node;
	}
}
