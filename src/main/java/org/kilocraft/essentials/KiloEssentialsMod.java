package org.kilocraft.essentials;

import net.fabricmc.api.DedicatedServerModInitializer;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.threaded.ThreadedKiloEssentialsMod;
import org.kilocraft.essentials.user.punishment.PunishmentManager;

public class KiloEssentialsMod implements DedicatedServerModInitializer {
	
	public static PunishmentManager punishmentsManager;
	
	@Override
    public void onInitializeServer() {
        ModConstants.getLogger().debug("Initializing KiloEssentials");
        ThreadManager.setMainName("KiloEssentials");
        ThreadManager mainThread = new ThreadManager(new ThreadedKiloEssentialsMod());
        mainThread.setMainThread(true);
        mainThread.start();
    }
}
