package org.kilocraft.essentials.craft.provider;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.world.biome.Biome;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kilocraft.essentials.craft.threaded.KiloThread;

public class ThreadedBiomeLocator implements KiloThread, Runnable {
    private ServerCommandSource source;
    private Biome biome;

    public ThreadedBiomeLocator(ServerCommandSource commandSource, Biome biomeToFind) {
        source = commandSource;
        biome = biomeToFind;

        getLogger().info("Started thread BiomeLocator by %s for biome \"%s\"", source.getName(), LocateBiomeProvider.getBiomeName(biome));
    }

    @Override
    public String getName() {
        return "BiomeLocator";
    }

    @Override
    public void run() {
        LocateBiomeProvider.execute(source, biome);
    }

    @Override
    public Logger getLogger() {
        return LogManager.getFormatterLogger(getName());
    }
}
