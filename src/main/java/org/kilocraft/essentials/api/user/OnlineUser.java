package org.kilocraft.essentials.api.user;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public interface OnlineUser extends User {
    ServerPlayerEntity getPlayer();

    ServerCommandSource getCommandSource();
}
