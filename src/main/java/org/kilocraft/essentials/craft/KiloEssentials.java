package org.kilocraft.essentials.craft;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kilocraft.essentials.api.Mod;
import org.kilocraft.essentials.craft.config.CacheHandler;
import org.kilocraft.essentials.craft.config.DataHandler;
import org.kilocraft.essentials.craft.config.KiloConfig;

public class KiloEssentials {
	private static KiloEssentials INSTANCE;
	private static Logger logger = LogManager.getFormatterLogger("KiloEssentials");
	private KiloConfig config;
	private KiloEvents events;
	private KiloCommands commands;
	private DataHandler dataHandler;

	public KiloEssentials(KiloConfig config, KiloEvents events, KiloCommands commands, DataHandler dataHandler) {
		logger.info("Running KiloEssentials version " + Mod.getVersion());

		this.config = config;
		this.events = events;
		this.commands = commands;
		this.dataHandler = dataHandler;

		CacheHandler.handle(false);

	}

	public static KiloEssentials getInstance() {
		return INSTANCE;
	}

	public static Logger getLogger() {
		return logger;
	}

	public KiloConfig getConfig() {
		return config;
	}

	public KiloEvents getEvents() {
		return events;
	}

	public KiloCommands getCommands() {
		return commands;
	}

	public DataHandler getDataHandler() {
		return dataHandler;
	}
}
