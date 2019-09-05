package org.kilocraft.essentials.craft;

import net.fabricmc.api.DedicatedServerModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kilocraft.essentials.api.Mod;
import org.kilocraft.essentials.craft.config.ConfigHandler;
import org.kilocraft.essentials.craft.config.DataHandler;

public class KiloEssentials implements DedicatedServerModInitializer {
	public static Logger getLogger = LogManager.getFormatterLogger();

	@Override
	public void onInitializeServer() {
		getLogger.info("Running KiloEssentials version " + Mod.getVersion());

		ConfigHandler.handle();
		DataHandler.handle();

		KiloCommands.register(false);

	}
}
