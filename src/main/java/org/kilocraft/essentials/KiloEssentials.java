package org.kilocraft.essentials;

import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kilocraft.essentials.commands.KiloCommands;
import org.kilocraft.essentials.config.ConfigHandler;


public class KiloEssentials implements ModInitializer {
	public static Logger getLogger = LogManager.getFormatterLogger("KiloEssentials");

	@Override
	public void onInitialize() {
		Mod mod = new Mod();
		getLogger.info("Loading KiloEssentials...");
		KiloCommands.register();
		ConfigHandler.handle();
	}
}
