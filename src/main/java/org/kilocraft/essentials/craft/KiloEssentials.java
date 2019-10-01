package org.kilocraft.essentials.craft;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kilocraft.essentials.api.Mod;
import org.kilocraft.essentials.craft.config.DataHandler;
import org.kilocraft.essentials.craft.config.KiloConifg;
import org.kilocraft.essentials.craft.registry.ConfigurableFeatures;
import org.kilocraft.essentials.craft.threaded.ThreadedKiloConfig;

public class KiloEssentials {
	public static KiloEssentials INSTANCE;
	private static Logger logger = LogManager.getFormatterLogger("KiloEssentials");
	private KiloEvents events;
	private KiloCommands commands;
	private DataHandler dataHandler;
	private ConfigurableFeatures configurableFeatures;
	//Threads
	ThreadManager kiloConfigThread;

	public KiloEssentials(KiloEvents events, KiloCommands commands, DataHandler dataHandler) {
		kiloConfigThread = new ThreadManager(new ThreadedKiloConfig());
		kiloConfigThread.start();

		this.events = events;
		this.commands = commands;
		this.dataHandler = dataHandler;

		logger.info("Running KiloEssentials version " + Mod.getVersion());


		/**
		 * IN
		 * @TEST
		 */

		//this.configurableFeatures = new ConfigurableFeatures();

		//this.configurableFeatures.tryToRegister(new WarpManager(), "Warps");
		//this.configurableFeatures.tryToRegister(new CustomCommands(), "CustomCommands");

		//configurableFeatures.close();
	}

	public static Logger getLogger() {
		return logger;
	}

	public KiloConifg getConfig() {
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
