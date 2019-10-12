package org.kilocraft.essentials.craft;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kilocraft.essentials.api.Mod;
import org.kilocraft.essentials.craft.config.KiloConifg;
import org.kilocraft.essentials.craft.data.KiloData;
import org.kilocraft.essentials.craft.homesystem.PlayerHomeManager;
import org.kilocraft.essentials.craft.registry.ConfigurableFeatures;

public class KiloEssentials {
	public static KiloEssentials INSTANCE;
	private static Logger logger = LogManager.getFormatterLogger("KiloEssentials");
	private KiloEvents events;
	private KiloCommands commands;
	private KiloData data;
	private ConfigurableFeatures configurableFeatures;
	//Threads
	ThreadManager kiloConfigThread;

	public KiloEssentials(KiloEvents events, KiloCommands commands, KiloData data) {
		logger.info("Running KiloEssentials version " + Mod.getVersion());

		new KiloConifg();

		this.events = events;
		this.commands = commands;
		this.data = data;

		ConfigurableFeatures features = new ConfigurableFeatures();
		features.tryToRegister(new PlayerHomeManager(), "PlayerHomes");

		KiloConifg.load();
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
