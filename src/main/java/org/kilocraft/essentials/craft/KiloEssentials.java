package org.kilocraft.essentials.craft;

import net.fabricmc.api.DedicatedServerModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kilocraft.essentials.api.Mod;
import org.kilocraft.essentials.craft.config.CacheHandler;
import org.kilocraft.essentials.craft.config.DataHandler;
import org.kilocraft.essentials.craft.config.KiloConfig;

public class KiloEssentials implements DedicatedServerModInitializer {
	private static KiloEssentials INSTANCE;
	private Logger logger = LogManager.getFormatterLogger();

	@Override
	public void onInitializeServer() {
		logger.info("Running KiloEssentials version " + Mod.getVersion());

		new KiloConfig();
		CacheHandler.handle(false);
		new KiloEvents();
		new KiloCommands(KiloConfig.getGeneral().get("Dev.environment"));

		new DataHandler();
		//PlayerHomeManager playerHomeManager = new PlayerHomeManager();
		//playerHomeManager.addHome(new Home("T-E-S-T", "player192", new BlockPos(1, 1, 1), 2.0, 1.0));
	}

	public Logger getLogger() {
		return logger;
	}

	public static KiloEssentials getInstance() {
		return INSTANCE;
	}
}
