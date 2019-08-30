package org.kilocraft.essentials;

import net.fabricmc.api.DedicatedServerModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kilocraft.essentials.commands.KiloCommands;
import org.kilocraft.essentials.config.ConfigHandler;

public class KiloEssentials implements DedicatedServerModInitializer {
	public static Logger getLogger = LogManager.getFormatterLogger("KiloEssentials");

	@Override
	public void onInitializeServer() {
		Mod mod = new Mod();
		getLogger.info("Loading KiloEssentials...");
		KiloCommands.register();
		ConfigHandler.handle();

	}
}
