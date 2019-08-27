package org.kilocraft.essentials;

import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kilocraft.essentials.config.ConfigFileHelper;


public class KiloEssentials implements ModInitializer {
	public static Logger getLogger = LogManager.getFormatterLogger("KiloEssentials-LOGGER-INSTANCE");

	@Override
	public void onInitialize() {
		new Mod();
		getLogger.info("Loading KiloEssentials...");

		ConfigFileHelper.loadConifgFiles("General.properties");


		new KiloCommands();
	}
}
