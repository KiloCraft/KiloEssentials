package org.kilocraft.essentials;

import net.fabricmc.api.DedicatedServerModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kilocraft.essentials.config.ConfigHandler;
import org.kilocraft.essentials.config.DataHandler;
import org.kilocraft.essentials.utils.ServerModName;

public class KiloEssentials implements DedicatedServerModInitializer {
	public static Logger getLogger = LogManager.getFormatterLogger("KiloEssentials");

	@Override
	public void onInitializeServer() {
		Mod mod = new Mod();
		getLogger.info("Running KiloEssentials version " + Mod.getVersion());
		ServerModName.setName("§bKiloCraft§r");

		ConfigHandler.handle();
		DataHandler.handle();

		KiloCommands.register(true);

	}
}
