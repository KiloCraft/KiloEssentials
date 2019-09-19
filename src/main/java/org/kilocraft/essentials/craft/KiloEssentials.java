package org.kilocraft.essentials.craft;

import net.fabricmc.api.DedicatedServerModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kilocraft.essentials.api.Mod;
import org.kilocraft.essentials.craft.config.DataHandler;
import org.kilocraft.essentials.craft.config.KiloConfig;

public class KiloEssentials implements DedicatedServerModInitializer {
	public static Logger getLogger = LogManager.getFormatterLogger();

	private String string;

	@Override
	public void onInitializeServer() {
		getLogger.info("Running KiloEssentials version " + Mod.getVersion());

		new KiloConfig();
		DataHandler.handle(false);
		new KiloEvents();
		new KiloCommands(KiloConfig.getGeneral().get("Dev.environment"));

	}
}
