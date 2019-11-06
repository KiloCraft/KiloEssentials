package org.kilocraft.essentials.craft;

import io.github.indicode.fabric.permissions.PermChangeBehavior;
import io.github.indicode.fabric.permissions.Thimble;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kilocraft.essentials.api.Mod;
import org.kilocraft.essentials.craft.config.KiloConifg;
import org.kilocraft.essentials.craft.registry.ConfigurableFeatures;
import org.kilocraft.essentials.craft.user.UserHomeHandler;
import org.kilocraft.essentials.craft.worldwarps.WarpManager;

import java.util.ArrayList;
import java.util.List;

public class KiloEssentials {
	private static Logger logger = LogManager.getFormatterLogger("KiloEssentials");
	private KiloEvents events;
	private KiloCommands commands;
	private ConfigurableFeatures configurableFeatures;
	private static List<String> initializedPerms = new ArrayList<>();

	public KiloEssentials(KiloEvents events, KiloCommands commands) {
		logger.info("Running KiloEssentials version " + Mod.getVersion());

		new KiloConifg();

		this.events = events;
		this.commands = commands;

		ConfigurableFeatures features = new ConfigurableFeatures();
		features.tryToRegister(new UserHomeHandler(), "PlayerHomes");
		features.tryToRegister(new WarpManager(), "ServerWideWarps");
		
		/**
		 * Initializing the permissions
		 */

		Thimble.permissionWriters.add((map, server) -> initializedPerms.forEach(perm -> map.registerPermission("kiloessentials." + perm, PermChangeBehavior.UPDATE_COMMAND_TREE)));

	}

	public static Logger getLogger() {
		return logger;
	}

	public KiloEvents getEvents() {
		return events;
	}

	public KiloCommands getCommands() {
		return commands;
	}

	public static String getPermissionFor(String node) {
		if (!initializedPerms.contains(node))
			initializedPerms.add(node);
		return "kiloessentials." + node;
	}
}
