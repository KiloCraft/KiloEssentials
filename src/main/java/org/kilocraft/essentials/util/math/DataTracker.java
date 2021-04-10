package org.kilocraft.essentials.util.math;

import net.minecraft.server.MinecraftServer;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.ModConstants;

public class DataTracker {

    public static final Average tps = new Average(28800);

    public static double getMSPT() {
        MinecraftServer server = KiloEssentials.getServer().getMinecraftServer();
        Average a = new Average(100).setData(server.lastTickLengths).setIndex(server.getTicks());
        return a.getAverage() / (1000000L);
    }

    public static String getFormattedMSPT() {
        MinecraftServer server = KiloEssentials.getServer().getMinecraftServer();
        Average a = new Average(100).setData(server.lastTickLengths).setIndex(server.getTicks());
        return ModConstants.DECIMAL_FORMAT.format(a.getAverage() / (1000000L));
    }

}
