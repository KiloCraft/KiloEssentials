package org.kilocraft.essentials.listeners;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.KiloDebugUtils;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.server.lifecycle.ServerScheduledUpdateEvent;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.chat.MutableTextMessage;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.user.UserHomeHandler;
import org.kilocraft.essentials.util.LocationUtil;
import org.kilocraft.essentials.util.registry.RegistryUtils;

public class OnScheduledUpdate implements EventHandler<ServerScheduledUpdateEvent> {
    @Override
    public void handle(@NotNull ServerScheduledUpdateEvent event) {
        KiloServer.getServer().getMetaManager().updateAll();

        if (KiloDebugUtils.shouldTick()) {
            KiloDebugUtils.INSTANCE.onTick();
        }

        for (ServerPlayerEntity player : KiloServer.getServer().getPlayerManager().getPlayerList()) {
            processDimension(player);
        }
    }

    private void processDimension(ServerPlayerEntity player) {
        boolean kickFromDim = KiloConfig.main().world().kickFromDimension;

        if (kickFromDim && LocationUtil.shouldBlockAccessTo(player.getServerWorld().getDimension()) && player.getServer() != null) {
            BlockPos pos = player.getSpawnPointPosition();
            DimensionType dim = RegistryUtils.toDimension(player.getSpawnPointDimension());

            if (pos == null) {
                OnlineUser user = KiloServer.getServer().getOnlineUser(player);
                if (user.getLastSavedLocation() != null) {
                    pos = user.getLastSavedLocation().toPos();
                    if (pos == null) {
                        UserHomeHandler homeHandler = user.getHomesHandler();
                        assert homeHandler != null;
                        if (homeHandler.getHomes().get(0) != null && homeHandler.getHomes().get(0).getLocation().getDimensionType() != player.getServerWorld().getDimension()) {
                            pos = user.getHomesHandler().getHomes().get(0).getLocation().toPos();
                        }
                    }
                }
            }

            if (pos != null) {
                player.teleport(RegistryUtils.toServerWorld(dim), pos.getX(), pos.getY(), pos.getZ(), player.yaw, player.pitch);
                KiloServer.getServer().getOnlineUser(player).sendMessage(String.format(KiloConfig.main().world().kickOutMessage, RegistryUtils.dimensionToName(player.getServerWorld().getDimension())));
            }
        }

    }
}
