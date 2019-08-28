package org.kilocraft.essentials;

import org.kilocraft.essentials.commands.KiloCommands;

import net.fabricmc.api.ModInitializer;

public class Init implements ModInitializer {
	
	KiloCommands kiloCommands;
	
    @Override
    public void onInitialize() {
        KiloEssentials.getLogger.info("KiloEssentials is loading...");
        kiloCommands = new KiloCommands();
    }
}
