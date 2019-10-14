package org.kilocraft.essentials.craft;

import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kilocraft.essentials.api.Mod;
import org.kilocraft.essentials.craft.config.KiloConifg;
import org.kilocraft.essentials.craft.data.KiloData;
import org.kilocraft.essentials.craft.homesystem.Home;
import org.kilocraft.essentials.craft.homesystem.HomeManager;
import org.kilocraft.essentials.craft.registry.ConfigurableFeatures;
import org.kilocraft.essentials.craft.worldwarps.WarpManager;

public class KiloEssentials {
	public static KiloEssentials INSTANCE;
	private static Logger logger = LogManager.getFormatterLogger("KiloEssentials");
	private KiloEvents events;
	private KiloCommands commands;
	private KiloData data;
	private ConfigurableFeatures configurableFeatures;

	public KiloEssentials(KiloEvents events, KiloCommands commands, KiloData data) {
		logger.info("Running KiloEssentials version " + Mod.getVersion());

		new KiloConifg();
		KiloConifg.load();

		this.events = events;
		this.commands = commands;
		this.data = data;

		ConfigurableFeatures features = new ConfigurableFeatures();
		features.tryToRegister(new HomeManager(), "PlayerHomes");
		features.tryToRegister(new WarpManager(), "ServerWideWarps");

		HomeManager.addHome(
				new Home(
						"#######",
						"test",
						new BlockPos(1, 20, 1),
						1F,
						5F
				)
		);

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
