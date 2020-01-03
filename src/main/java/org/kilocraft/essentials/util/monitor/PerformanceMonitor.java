package org.kilocraft.essentials.util.monitor;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.dimension.DimensionType;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.server.Server;

public class PerformanceMonitor {
    private Server server;
    private ServerWorld WORLD;
    private ServerWorld WORLD_NETHER;
    private ServerWorld WORLD_THE_END;
    private OverworldInfo overworldInfo;
    private NetherInfo netherInfo;
    private TheEndInfo theEndInfo;

    public PerformanceMonitor() {
        this.server = KiloServer.getServer();
        WORLD = server.getVanillaServer().getWorld(DimensionType.OVERWORLD);
        WORLD_NETHER = server.getVanillaServer().getWorld(DimensionType.THE_NETHER);
        WORLD_THE_END = server.getVanillaServer().getWorld(DimensionType.THE_END);

        overworldInfo = new OverworldInfo(WORLD);
        netherInfo = new NetherInfo(WORLD_NETHER);
        theEndInfo = new TheEndInfo(WORLD_THE_END);
    }

    public OverworldInfo getOverworldInfo() {
        return overworldInfo;
    }

    public NetherInfo getNetherInfo() {
        return netherInfo;
    }

    public TheEndInfo getTheEndInfo() {
        return theEndInfo;
    }

}
