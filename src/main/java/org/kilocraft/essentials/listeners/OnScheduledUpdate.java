package org.kilocraft.essentials.listeners;

import net.minecraft.SharedConstants;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import org.kilocraft.essentials.KiloDebugUtils;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.server.lifecycle.ServerScheduledUpdateEvent;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.chat.ChatMessage;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.extensions.betterchairs.PlayerSitManager;
import org.kilocraft.essentials.user.UserHomeHandler;
import org.kilocraft.essentials.util.LocationUtil;

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
        boolean kickFromDim = KiloConfig.main().world().kickFromDimension;

        if (kickFromDim && !LocationUtil.isDimensionValid(player.dimension) && player.getServer() != null) {
            BlockPos pos = player.getSpawnPosition();
            if (pos == null) {
                OnlineUser user = KiloServer.getServer().getOnlineUser(player);
                if (user.getLastSavedLocation() != null) {
                    pos = user.getLastSavedLocation().toPos();
                    if (pos == null) {
                        UserHomeHandler homeHandler = user.getHomesHandler();
                        if (homeHandler.getHomes().get(0) != null && homeHandler.getHomes().get(0).getLocation().getDimensionType() != player.dimension) {
                            pos = user.getHomesHandler().getHomes().get(0).getLocation().toPos();
                        }
                    }
                }
            }

            if (pos != null) {
                player.teleport(player.getServer().getWorld(DimensionType.OVERWORLD), pos.getX(), pos.getY(), pos.getZ(), player.yaw, player.pitch);
                KiloChat.sendMessageTo(player, new ChatMessage(String.format(KiloConfig.main().world().kickOutMessage, player.dimension.toString()), true));
            }
        }

    }
}
