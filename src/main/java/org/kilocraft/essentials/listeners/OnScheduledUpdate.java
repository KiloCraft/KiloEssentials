package org.kilocraft.essentials.listeners;

import net.minecraft.SharedConstants;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.dimension.DimensionType;
import org.kilocraft.essentials.KiloDebugUtils;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.server.lifecycle.ServerScheduledUpdateEvent;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.extensions.betterchairs.PlayerSitManager;

public class OnScheduledUpdate implements EventHandler<ServerScheduledUpdateEvent> {
    @Override
    public void handle(ServerScheduledUpdateEvent event) {
        KiloServer.getServer().getMetaManager().updateAll();
        PlayerSitManager.INSTANCE.onScheduledUpdate();

        if (SharedConstants.isDevelopment) {
            KiloDebugUtils.INSTANCE.onScheduledUpdate();
        }

        for (ServerPlayerEntity player : KiloServer.getServer().getPlayerManager().getPlayerList()) {
            processDimension(player);
        }
    }

    private void processDimension(ServerPlayerEntity player) {
        boolean allowNether = KiloConfig.main().world().allowTheNether;
        boolean allowTheEnd = KiloConfig.main().world().allowTheEnd;
        boolean kickFromDim = KiloConfig.main().world().kickFromDimension;

        if (kickFromDim &&
                (!allowNether && player.getEntityWorld().getDimension().getType().equals(DimensionType.THE_NETHER)) ||
                !allowTheEnd && player.getEntityWorld().getDimension().getType().equals(DimensionType.THE_END))
            player.requestRespawn();

    }
}
