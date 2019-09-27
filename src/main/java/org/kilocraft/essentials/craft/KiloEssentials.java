package org.kilocraft.essentials.craft;

import net.fabricmc.api.DedicatedServerModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kilocraft.essentials.api.Mod;
import org.kilocraft.essentials.craft.config.CacheHandler;
import org.kilocraft.essentials.craft.config.KiloConfig;
import org.kilocraft.essentials.craft.homesystem.PlayerHomeManager;

public class KiloEssentials implements DedicatedServerModInitializer {
	public static Logger getLogger = LogManager.getFormatterLogger();

	@Override
	public void onInitializeServer() {
		getLogger.info("Running KiloEssentials version " + Mod.getVersion());

		new KiloConfig();
		CacheHandler.handle(false);
		new KiloEvents();
		new KiloCommands(KiloConfig.getGeneral().get("Dev.environment"));

		//new DataHandler();
		PlayerHomeManager playerHomeManager = new PlayerHomeManager();
	}
}
