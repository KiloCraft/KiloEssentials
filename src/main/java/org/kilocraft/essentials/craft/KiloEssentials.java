package org.kilocraft.essentials.craft;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kilocraft.essentials.api.Mod;
import org.kilocraft.essentials.craft.homesystem.HomeManager;
import org.kilocraft.essentials.craft.registry.ConfigurableFeatures;
import org.kilocraft.essentials.craft.threaded.ThreadedKiloConfig;
import org.kilocraft.essentials.craft.worldwarps.WarpManager;

public class KiloEssentials {
	private static Logger logger = LogManager.getFormatterLogger("KiloEssentials");
	private KiloEvents events;
	private KiloCommands commands;
	private ConfigurableFeatures configurableFeatures;
	private ThreadManager configThread;

	public KiloEssentials(KiloEvents events, KiloCommands commands) {
		logger.info("Running KiloEssentials version " + Mod.getVersion());

		configThread = new ThreadManager(new ThreadedKiloConfig());
		configThread.start();

		this.events = events;
		this.commands = commands;

		ConfigurableFeatures features = new ConfigurableFeatures();
		features.tryToRegister(new HomeManager(), "PlayerHomes");
		features.tryToRegister(new WarpManager(), "ServerWideWarps");

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
}
