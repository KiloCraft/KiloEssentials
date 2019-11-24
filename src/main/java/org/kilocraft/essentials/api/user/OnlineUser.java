package org.kilocraft.essentials.api.user;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.api.KiloServer;

public interface OnlineUser extends User {
    public ServerPlayerEntity getPlayer();

    public ServerCommandSource getCommandSource();
}
